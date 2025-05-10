package com.example.middleware.rabbitmq;

import com.alibaba.fastjson2.JSON;
import com.example.middleware.rabbitmq.config.RabbitMQConfig;
import com.example.middleware.rabbitmq.factory.ChannelFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.GetResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * RabbitMQ工具类，提供消息队列的各种操作
 * 包括：消息发送、接收、队列管理等功能
 */
@Slf4j
@Component
public class RabbitMQUtil {
    private final RabbitMQConfig rabbitMQConfig;
    private String currentQueueName;
    private GenericObjectPool<Channel> channelPool;
    private Connection connection;



    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 将对象转换为JSON字符串
     *
     * @param obj 要转换的对象
     * @return JSON字符串
     */
    public static String toJsonString(Object obj) {
        return JSON.toJSONString(obj);
    }
    /**
     * 将JSON字符串转换为指定类型的对象
     *
     * @param jsonString JSON字符串
     * @param clazz      目标类型
     * @return 转换后的对象
     */
    public static <T> T toObject(String jsonString, Class<T> clazz) {
        return JSON.parseObject(jsonString, clazz);
    }
    /**
     * 构造函数，注入RabbitMQ配置
     *
     * @param rabbitMQConfig RabbitMQ配置
     */
    public RabbitMQUtil(RabbitMQConfig rabbitMQConfig) {
        this.rabbitMQConfig = rabbitMQConfig;
        this.currentQueueName = rabbitMQConfig.getProperties().getQueueName();
    }

    /**
     * 初始化RabbitMQ连接和通道池
     * 在Spring容器启动时自动执行
     */
    @PostConstruct
    public void init() {
        try {
            // 初始化连接
            connection = rabbitMQConfig.rabbitConnectionFactory().newConnection();
            log.info("RabbitMQ连接已建立: {}:{}",
                    rabbitMQConfig.getProperties().getHost(),
                    rabbitMQConfig.getProperties().getPort());
            // 配置连接池
            GenericObjectPoolConfig<Channel> poolConfig = new GenericObjectPoolConfig<>();
            poolConfig.setMaxTotal(10);
            poolConfig.setMaxIdle(5);
            poolConfig.setMinIdle(1);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTimeBetweenEvictionRunsMillis(30000); // 每30秒运行一次空闲对象回收器

            // 创建通道池
            channelPool = new GenericObjectPool<>(new ChannelFactory(connection), poolConfig);
            log.info("RabbitMQ通道池已初始化，最大连接数: {}", poolConfig.getMaxTotal());
        } catch (IOException | TimeoutException e) {
            log.error("初始化RabbitMQ连接失败", e);
            throw new RuntimeException("初始化RabbitMQ连接失败", e);
        }
    }
    public void sendMessage(String message){
        sendMessage(message,currentQueueName);
    }
    public void sendMessage(String message,String queueName){
        rabbitTemplate.convertAndSend(queueName,message);

    }
    /**
     * 不带参数的接收消息方法，使用默认队列名和参数
     * 
     * @return 接收到的消息内容
     */
    public String receiveMessage() {
        return receiveMessage(currentQueueName, 1, 5000);
    }

    /**
     * 从指定队列接收消息，使用默认的消息数量和超时时间
     * 
     * @param queueName 队列名称
     * @return 接收到的消息内容，如果没有消息则返回null
     */
    public String receiveMessage(String queueName) {
        return receiveMessage(queueName, 1, 5000);
    }
    
    /**
     * 接收指定队列中的消息
     * 
     * @param queueName 队列名称
     * @param nums 要接收的消息数量
     * @param timeoutMills 超时时间(毫秒)
     * @return 接收到的消息内容，如果没有消息则返回null
     */
    // 在receiveMessage方法开始处添加一个唯一标识符，帮助跟踪调用
    public String receiveMessage(String queueName, int nums, long timeoutMills) {
        try {
            if (nums <= 0) {
                log.warn("接收消息数量必须大于0，已自动调整为1");
                nums = 1;
            }
            
            // 使用通道池中的通道进行精确控制
            Channel channel = null;
            try {
                channel = channelPool.borrowObject();
                
                // 确保队列存在
                AMQP.Queue.DeclareOk queueInfo = channel.queueDeclarePassive(queueName);
                int messageCount = queueInfo.getMessageCount();
                log.debug("队列 {} 当前消息数量: {}", queueName, messageCount);
                
                // 接收指定数量的消息
                StringBuilder messages = new StringBuilder();
                int actualReceived = 0;
                for (int i = 0; i < nums; i++) {
                    GetResponse response = channel.basicGet(queueName, false); // 手动确认模式
                    if (response != null) {
                        AMQP.BasicProperties props = response.getProps();
                        String messageId = props.getMessageId();
                        long deliveryTag = response.getEnvelope().getDeliveryTag(); // 获取投递标签，用于确认
                        actualReceived++;
                        String messageContent = new String(response.getBody(), StandardCharsets.UTF_8);
                        
                        // 将详细日志改为debug级别
                        log.debug("接收到消息 ID: {}, 队列剩余: {}, deliveryTag: {}", 
                                messageId != null ? messageId : "未设置", 
                                response.getMessageCount(),
                                deliveryTag);
                                
                        if (messages.length() > 0) {
                            messages.append("\n");
                        }
                        messages.append(messageContent);
                        try {
                            channel.basicAck(deliveryTag, false); // 确认消息，false 表示只确认当前消息
                        } catch (IOException e) {
                            log.error("确认消息失败，DeliveryTag: {}", deliveryTag, e);
                        }
                        
                        if (actualReceived >= nums) {
                            break;
                        }
                    } else {
                        log.debug("队列 {} 中没有更多可用消息，已接收 {}/{} 条", queueName, actualReceived, nums);
                        break;
                    }
                }
                
                // 保留关键业务日志为info级别
                if (actualReceived > 0) {
                    log.info("从队列 {} 接收了 {} 条消息", queueName, actualReceived);
                } else {
                    log.debug("从队列 {} 未接收到消息", queueName);
                }
                
                return messages.length() > 0 ? messages.toString() : null;
            } finally {
                if (channel != null) {
                    channelPool.returnObject(channel);
                }
            }
        } catch (Exception e) {
            log.error("从队列 {} 接收消息时发生错误: {}", queueName, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 将接收到的消息对象转换为字符串
     * 
     * @param obj 接收到的消息对象
     * @return 转换后的字符串
     */
    private String convertToString(Object obj) {
        if (obj == null) {
            return null;
        }
        
        // 处理字节数组
        if (obj instanceof byte[]) {
            return new String((byte[]) obj, StandardCharsets.UTF_8);
        }
        
        // 处理Message对象
        if (obj instanceof Message) {
            byte[] body = ((Message) obj).getBody();
            return new String(body, StandardCharsets.UTF_8);
        }
        
        // 其他类型直接转字符串
        return obj.toString();
    }

    public void setQueueName(String testQueue) {
        this.currentQueueName=testQueue;

    }

    /**
     * 发送对象消息到默认队列
     *
     * @param object 要发送的对象
     * @param <T> 对象类型
     */
    public <T> void sendObject(T object) {
        sendObject(object, currentQueueName);
    }

    /**
     * 发送对象消息到指定队列
     *
     * @param object 要发送的对象
     * @param queueName 目标队列名称
     * @param <T> 对象类型
     */
    public <T> void sendObject(T object, String queueName) {
        try {
            String jsonMessage = toJsonString(object);
            sendMessage(jsonMessage, queueName);
            log.debug("对象已序列化并发送到队列: {}, 类型: {}", queueName, object.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("发送对象到队列 {} 失败: {}", queueName, e.getMessage(), e);
            throw new RuntimeException("对象消息发送失败", e);
        }
    }

    /**
     * 从默认队列接收并转换为指定类型的对象
     *
     * @param clazz 目标类型
     * @param <T> 对象类型
     * @return 转换后的对象，如果没有消息则返回null
     */
    public <T> T receiveObject(Class<T> clazz) {
        return receiveObject(currentQueueName, clazz, 1, 5000);
    }

    /**
     * 从指定队列接收并转换为指定类型的对象
     *
     * @param queueName 队列名称
     * @param clazz 目标类型
     * @param <T> 对象类型
     * @return 转换后的对象，如果没有消息则返回null
     */
    public <T> T receiveObject(String queueName, Class<T> clazz) {
        return receiveObject(queueName, clazz, 1, 5000);
    }

    /**
     * 从指定队列接收并转换为指定类型的对象
     *
     * @param queueName 队列名称
     * @param clazz 目标类型
     * @param nums 要接收的消息数量
     * @param timeoutMills 超时时间(毫秒)
     * @param <T> 对象类型
     * @return 转换后的对象，如果没有消息则返回null
     */
    public <T> T receiveObject(String queueName, Class<T> clazz, int nums, long timeoutMills) {
        String jsonMessage = receiveMessage(queueName, nums, timeoutMills);
        if (jsonMessage == null) {
            return null;
        }
        
        try {
            T object = toObject(jsonMessage, clazz);
            log.debug("从队列 {} 接收到消息并反序列化为: {}", queueName, clazz.getSimpleName());
            return object;
        } catch (Exception e) {
            log.error("从队列 {} 接收的消息反序列化失败: {}", queueName, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 使用指定的RoutingKey发送消息到默认交换机
     *
     * @param message 消息内容
     * @param routingKey 路由键
     */
    public void sendMessageWithRoutingKey(String message, String routingKey) {
        try {
            rabbitTemplate.convertAndSend("", routingKey, message);
            log.debug("消息已发送到默认交换机，RoutingKey: {}", routingKey);
        } catch (Exception e) {
            log.error("使用RoutingKey发送消息失败: {}", e.getMessage(), e);
            throw new RuntimeException("消息发送失败", e);
        }
    }
    
    /**
     * 使用指定的交换机和RoutingKey发送消息
     *
     * @param exchange 交换机名称
     * @param routingKey 路由键
     * @param message 消息内容
     */
    public void sendMessageWithExchange(String exchange, String routingKey, String message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.debug("消息已发送到交换机: {}, RoutingKey: {}", exchange, routingKey);
        } catch (Exception e) {
            log.error("发送消息到交换机 {} 失败: {}", exchange, e.getMessage(), e);
            throw new RuntimeException("消息发送失败", e);
        }
    }
    
    /**
     * 使用指定的交换机和RoutingKey发送对象消息
     *
     * @param exchange 交换机名称
     * @param routingKey 路由键
     * @param object 要发送的对象
     * @param <T> 对象类型
     */
    public <T> void sendObjectWithExchange(String exchange, String routingKey, T object) {
        try {
            String jsonMessage = toJsonString(object);
            sendMessageWithExchange(exchange, routingKey, jsonMessage);
            log.debug("对象已序列化并发送到交换机: {}, RoutingKey: {}, 类型: {}", 
                    exchange, routingKey, object.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("发送对象到交换机 {} 失败: {}", exchange, e.getMessage(), e);
            throw new RuntimeException("对象消息发送失败", e);
        }
    }
    
    /**
     * 使用指定的RoutingKey发送对象消息到默认交换机
     *
     * @param routingKey 路由键
     * @param object 要发送的对象
     * @param <T> 对象类型
     */
    public <T> void sendObjectWithRoutingKey(String routingKey, T object) {
        try {
            String jsonMessage = toJsonString(object);
            sendMessageWithRoutingKey(jsonMessage, routingKey);
            log.debug("对象已序列化并发送，RoutingKey: {}, 类型: {}", 
                    routingKey, object.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("使用RoutingKey发送对象失败: {}", e.getMessage(), e);
            throw new RuntimeException("对象消息发送失败", e);
        }
    }
    
    /**
     * 发送带类型标识和RoutingKey的对象消息
     *
     * @param exchange 交换机名称
     * @param routingKey 路由键
     * @param object 要发送的对象
     * @param type 消息类型标识
     * @param <T> 对象类型
     */
    public <T> void sendTypedObjectWithRouting(String exchange, String routingKey, T object, String type) {
        try {
            // 创建包含类型标识的消息结构
            String objectJson = toJsonString(object);
            String typedJson = "{\"type\":\"" + type + "\",\"data\":" + objectJson + "}";
            
            sendMessageWithExchange(exchange, routingKey, typedJson);
            log.debug("带类型标识的对象已发送到交换机: {}, RoutingKey: {}, 类型: {}", 
                    exchange, routingKey, type);
        } catch (Exception e) {
            log.error("发送带类型标识的对象到交换机 {} 失败: {}", exchange, e.getMessage(), e);
            throw new RuntimeException("带类型标识的对象消息发送失败", e);
        }
    }
    
    /**
     * 关于RabbitMQ交换机、路由键和队列的创建问题
     *
     * 您提出了一个很好的问题。确实，使用交换机和路由键的方式看起来可能有些复杂，而且您关心是否需要提前创建这些组件。
     *
     * ## 是否太复杂？
     *
     * RabbitMQ的交换机-路由键-队列模型确实比直接使用队列要复杂一些，但这种复杂性带来了更大的灵活性：
     *
     * 1. **简单使用**：如果您只需要基本的消息发送和接收功能，可以只使用默认交换机和队列，不必关心路由键。
     * 2. **高级使用**：当您需要更复杂的消息路由逻辑时，交换机和路由键提供了强大的功能。
     *
     * ## 是否需要提前创建？
     *
     * 关于是否需要提前创建交换机、队列和绑定关系：
     *
     * 1. **自动创建**：Spring AMQP提供了自动创建这些组件的功能。您可以在配置类中定义Bean，Spring会在应用启动时自动创建它们。
     *
     * 2. **按需创建**：我们可以在RabbitMQUtil中添加方法，在需要时动态创建这些组件。
     *
     * ## 简化方案
     *
     * 我建议添加一些辅助方法，使交换机和路由键的使用更加简单：
     */
    /**
     * 确保队列存在，如果不存在则创建
     * 
     * @param queueName 队列名称
     */
    public void ensureQueueExists(String queueName) {
        try {
            Channel channel = channelPool.borrowObject();
            try {
                // 声明队列，如果不存在则创建
                // durable=true 持久化队列
                // exclusive=false 非排他队列
                // autoDelete=false 不自动删除
                channel.queueDeclare(queueName, true, false, false, null);
                log.debug("确保队列 {} 存在", queueName);
            } finally {
                channelPool.returnObject(channel);
            }
        } catch (Exception e) {
            log.error("创建队列 {} 失败: {}", queueName, e.getMessage(), e);
            throw new RuntimeException("创建队列失败", e);
        }
    }
    
    /**
     * 创建交换机并绑定到队列
     * 
     * @param exchangeName 交换机名称
     * @param queueName 队列名称
     * @param routingKey 路由键
     * @param exchangeType 交换机类型 (direct, topic, fanout, headers)
     */
    public void createExchangeAndBinding(String exchangeName, String queueName, String routingKey, String exchangeType) {
        try {
            Channel channel = channelPool.borrowObject();
            try {
                // 确保队列存在
                channel.queueDeclare(queueName, true, false, false, null);
                
                // 声明交换机
                channel.exchangeDeclare(exchangeName, exchangeType, true);
                
                // 绑定队列到交换机
                channel.queueBind(queueName, exchangeName, routingKey);
                
                log.info("创建交换机 {} 并绑定到队列 {}, 路由键: {}", exchangeName, queueName, routingKey);
            } finally {
                channelPool.returnObject(channel);
            }
        } catch (Exception e) {
            log.error("创建交换机和绑定失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建交换机和绑定失败", e);
        }
    }
    
    /**
     * 简化版：使用Direct交换机发送消息
     * 自动创建交换机、队列和绑定关系
     * 
     * @param message 消息内容
     * @param exchangeName 交换机名称
     * @param routingKey 路由键
     */
    public void sendMessageWithDirectExchange(String message, String exchangeName, String routingKey) {
        try {
            // 确保交换机、队列和绑定关系存在
            createExchangeAndBinding(exchangeName, routingKey, routingKey, "direct");
            
            // 发送消息
            sendMessageWithExchange(exchangeName, routingKey, message);
        } catch (Exception e) {
            log.error("使用Direct交换机发送消息失败: {}", e.getMessage(), e);
            throw new RuntimeException("消息发送失败", e);
        }
    }
    
    /**
     * 简化版：使用Direct交换机发送对象消息
     * 自动创建交换机、队列和绑定关系
     * 
     * @param object 要发送的对象
     * @param exchangeName 交换机名称
     * @param routingKey 路由键
     */
    public <T> void sendObjectWithDirectExchange(T object, String exchangeName, String routingKey) {
        try {
            String jsonMessage = toJsonString(object);
            sendMessageWithDirectExchange(jsonMessage, exchangeName, routingKey);
            log.debug("对象已序列化并通过Direct交换机发送, 交换机: {}, 路由键: {}, 类型: {}", 
                    exchangeName, routingKey, object.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("使用Direct交换机发送对象失败: {}", e.getMessage(), e);
            throw new RuntimeException("对象消息发送失败", e);
        }
    }
    
    /**
     * 发送带类型标识的对象消息到指定队列
     *
     * @param object 要发送的对象
     * @param type 消息类型标识
     * @param queueName 目标队列名称
     * @param <T> 对象类型
     */
    public <T> void sendTypedObject(T object, String type, String queueName) {
        try {
            // 创建包含类型标识的消息结构
            String objectJson = toJsonString(object);
            String typedJson = "{\"type\":\"" + type + "\",\"data\":" + objectJson + "}";
            
            sendMessage(typedJson, queueName);
            log.debug("带类型标识的对象已发送到队列: {}, 类型: {}", queueName, type);
        } catch (Exception e) {
            log.error("发送带类型标识的对象到队列 {} 失败: {}", queueName, e.getMessage(), e);
            throw new RuntimeException("带类型标识的对象消息发送失败", e);
        }
    }
    
    /**
     * 检查消息是否为指定类型
     *
     * @param message 消息内容
     * @param type 要检查的类型
     * @return 如果消息是指定类型则返回true
     */
    public boolean isMessageOfType(String message, String type) {
        try {
            // 解析JSON获取类型字段
            com.alibaba.fastjson2.JSONObject jsonObject = JSON.parseObject(message);
            String messageType = jsonObject.getString("type");
            return type.equals(messageType);
        } catch (Exception e) {
            log.error("检查消息类型失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 从带类型标识的消息中提取数据部分
     *
     * @param message 带类型标识的消息
     * @return 提取的数据部分JSON字符串
     */
    public String extractDataFromTypedMessage(String message) {
        try {
            com.alibaba.fastjson2.JSONObject jsonObject = JSON.parseObject(message);
            return jsonObject.getJSONObject("data").toString();
        } catch (Exception e) {
            log.error("从消息中提取数据失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 从默认队列接收指定类型的对象
     *
     * @param type 消息类型
     * @param clazz 目标类型
     * @param <T> 对象类型
     * @return 转换后的对象，如果没有匹配类型的消息则返回null
     */
    public <T> T receiveTypedObject(String type, Class<T> clazz) {
        return receiveTypedObject(currentQueueName, type, clazz, 1, 5000);
    }
    
    /**
     * 从指定队列接收指定类型的对象
     *
     * @param queueName 队列名称
     * @param type 消息类型
     * @param clazz 目标类型
     * @param <T> 对象类型
     * @return 转换后的对象，如果没有匹配类型的消息则返回null
     */
    public <T> T receiveTypedObject(String queueName, String type, Class<T> clazz) {
        return receiveTypedObject(queueName, type, clazz, 1, 5000);
    }
    
    /**
     * 从指定队列接收指定类型的对象，可以尝试多次接收
     *
     * @param type 消息类型
     * @param clazz 目标类型
     * @param maxAttempts 最大尝试次数
     * @param timeoutMills 每次尝试的超时时间(毫秒)
     * @param <T> 对象类型
     * @return 转换后的对象，如果没有匹配类型的消息则返回null
     */
    public <T> T receiveTypedObject(String type, Class<T> clazz, int maxAttempts, long timeoutMills) {
        return receiveTypedObject(currentQueueName, type, clazz, maxAttempts, timeoutMills);
    }
    
    /**
     * 从指定队列接收指定类型的对象，可以尝试多次接收
     *
     * @param queueName 队列名称
     * @param type 消息类型
     * @param clazz 目标类型
     * @param maxAttempts 最大尝试次数
     * @param timeoutMills 每次尝试的超时时间(毫秒)
     * @param <T> 对象类型
     * @return 转换后的对象，如果没有匹配类型的消息则返回null
     */
    public <T> T receiveTypedObject(String queueName, String type, Class<T> clazz, int maxAttempts, long timeoutMills) {
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            String message = receiveMessage(queueName, 1, timeoutMills);
            if (message == null) {
                log.debug("第{}次尝试未接收到消息", attempt + 1);
                continue;
            }
            
            try {
                // 检查消息类型
                if (isMessageOfType(message, type)) {
                    // 提取数据部分
                    String dataJson = extractDataFromTypedMessage(message);
                    if (dataJson != null) {
                        // 转换为目标类型
                        T object = toObject(dataJson, clazz);
                        log.debug("成功接收到类型为{}的对象", type);
                        return object;
                    }
                } else {
                    log.debug("接收到的消息类型不匹配，期望: {}, 实际: {}", type, 
                            JSON.parseObject(message).getString("type"));
                }
            } catch (Exception e) {
                log.error("处理类型化消息失败: {}", e.getMessage(), e);
            }
        }
        
        log.debug("在{}次尝试后未找到类型为{}的消息", maxAttempts, type);
        return null;
    }
}
    
    // ... 现有代码 ...