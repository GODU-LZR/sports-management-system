package com.example.notification.utils;

import com.alibaba.fastjson2.JSON;
import com.example.notification.callback.MessageCallback;
import com.example.notification.config.RabbitMQConfig;
import com.example.notification.factory.ChannelFactory;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
@Slf4j
@Component
public class RabbitMQUtil {
    private final RabbitMQConfig rabbitMQConfig;
    private String currentQueueName;
    private GenericObjectPool<Channel> channelPool;
    private Connection connection;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 1000;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public static String toJsonString(Object obj) {
        return JSON.toJSONString(obj);
    }

    public static <T> T toObject(String jsonString, Class<T> clazz) {
        return JSON.parseObject(jsonString, clazz);
    }

    public RabbitMQUtil(RabbitMQConfig rabbitMQConfig) {
        this.rabbitMQConfig = rabbitMQConfig;
        this.currentQueueName = rabbitMQConfig.getProperties().getQueueName();
    }

    @PostConstruct
    public void init() throws IOException, TimeoutException {
        // 初始化连接
        connection = rabbitMQConfig.rabbitConnectionFactory().newConnection();

        // 配置连接池
        GenericObjectPoolConfig<Channel> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        poolConfig.setTestOnBorrow(true);

        // 创建通道池
        channelPool = new GenericObjectPool<>(new ChannelFactory(connection), poolConfig);

        // 初始化死信队列
        initializeDeadLetterQueue();
    }
//TODO死信初始化未完成
    private void initializeDeadLetterQueue() {
//        try (Channel channel = channelPool.borrowObject()) {
//            String deadLetterExchange = getDeadLetterExchangeName();
//            String deadLetterQueue = getDeadLetterQueueName();
//
//            channel.exchangeDeclare(deadLetterExchange, "direct", true);
//            channel.queueDeclare(deadLetterQueue, true, false, false, null);
//            channel.queueBind(deadLetterQueue, deadLetterExchange, "dead");
//        } catch (Exception e) {
//            log.error("初始化死信队列失败", e);
//            throw new RuntimeException("初始化死信队列失败", e);
//        }
    }

    private String getDeadLetterExchangeName() {
        return currentQueueName + ".dlx";
    }

    private String getDeadLetterQueueName() {
        return currentQueueName + ".dlq";
    }

    private Map<String, Object> getDeadLetterArguments() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", getDeadLetterExchangeName());
        arguments.put("x-dead-letter-routing-key", "dead");
        return arguments;
    }

    public void setQueueName(String queueName) {
        this.currentQueueName = queueName;
    }
    public void sendMessage(String message) {
        sendMessage(message,currentQueueName);
    }
    public void sendMessage(String message,String targetQueen) {
        int retryCount = 0;
        while (retryCount < MAX_RETRY_ATTEMPTS) {
            try {
                Channel channel = channelPool.borrowObject();
                try {
                    // 声明队列
                    Map<String, Object> arguments = new HashMap<>();
                    //死信队列配置有待处理
                    arguments.put("x-dead-letter-exchange", currentQueueName + ".dlx");
                    arguments.put("x-dead-letter-routing-key", "dead");

                    channel.queueDeclare(targetQueen,
                            rabbitMQConfig.getProperties().isDurable(),
                            rabbitMQConfig.getProperties().isExclusive(),
                            rabbitMQConfig.getProperties().isAutoDelete(),
                            arguments);

                    // 发送消息并等待确认
                    channel.basicPublish("", targetQueen,
                            MessageProperties.PERSISTENT_TEXT_PLAIN,
                            message.getBytes());
                    
                    if (channel.waitForConfirms()) {
                        log.info("消息发送成功: {}, 队列: {}", message, targetQueen);
                        return;
                    }
                } finally {
                    channelPool.returnObject(channel);
                }
            } catch (Exception e) {
                log.warn("发送消息失败，尝试重试 {}/{}: {}", retryCount + 1, MAX_RETRY_ATTEMPTS, e.getMessage());
                retryCount++;
                if (retryCount >= MAX_RETRY_ATTEMPTS) {
                    log.error("发送消息最终失败", e);
                    throw new RuntimeException("消息发送失败，已重试" + MAX_RETRY_ATTEMPTS + "次", e);
                }
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
            }
        }
    }

    /**
     * 接收消息
     * @param callback 消息处理回调
     */
    public void receiveMessage(MessageCallback callback) {
        receiveMessage(callback, currentQueueName);
    }

    /**
     * 接收消息
     * @param callback 消息处理回调
     * @param targetQueueName 目标队列名称
     */
    public void receiveMessage(MessageCallback callback, String targetQueueName) {
        try {
            Channel channel = channelPool.borrowObject();
            try {
                // 设置预取计数为1，确保消息公平分发
                channel.basicQos(1);
                
                // 消费消息
                channel.basicConsume(targetQueueName, false, (consumerTag, delivery) -> {
                    try {
                        String messageBody = new String(delivery.getBody());
                        callback.onMessage(messageBody);
                        // 手动确认消息
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    } catch (Exception e) {
                        log.error("处理消息时发生错误", e);
                        // 消息处理失败，拒绝消息并重新入队
                        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                    }
                }, consumerTag -> log.info("消费者已取消: {}", consumerTag));
            } catch (IOException e) {
                channelPool.returnObject(channel);
                throw new RuntimeException("设置消费者失败", e);
            }
        } catch (Exception e) {
            log.error("创建消费者失败", e);
            throw new RuntimeException("创建消费者失败", e);
        }
    }

    /**
     * 同步接收消息，带超时时间
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 消息内容，如果超时返回null
     */
    public String receiveMessage(long timeout, TimeUnit unit) {
        return   receiveMessage(timeout,unit,currentQueueName);

    }
    public String receiveMessage(long timeout, TimeUnit unit,String targetQueueName) {
        return rabbitTemplate.execute(channel -> {
            GetResponse response = channel.basicGet(targetQueueName, true);
            if (response != null) {
                return new String(response.getBody());
            }
            return null;
        });
    }

    @PreDestroy
    public void destroy() {
        try {
            if (channelPool != null) {
                channelPool.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (Exception e) {
            log.error("关闭RabbitMQ连接资源失败", e);
        }
    }
}
