package com.example.middleware.rabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder; // 推荐使用 Builder
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQQueueConfig { // 类名可自定义

    // 读取你在 application.properties 中定义的队列名
    @Value("${rabbitmq.queue.email}") 
    private String emailQueueName;

    @Bean
    public Queue emailVerificationQueue() {
        // 声明一个持久化的队列
        log.info("声明 RabbitMQ 队列: {}", emailQueueName); // 添加日志确认Bean被创建
        return QueueBuilder.durable(emailQueueName).build(); 
    }
}