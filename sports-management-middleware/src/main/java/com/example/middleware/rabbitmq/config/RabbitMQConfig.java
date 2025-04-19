package com.example.middleware.rabbitmq.config;

import com.rabbitmq.client.ConnectionFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(RabbitMQConfig.RabbitMQProperties.class)
public class RabbitMQConfig {

    @Autowired
    private RabbitMQProperties properties;

    @Bean("customRabbitConnectionFactory")
    public ConnectionFactory rabbitConnectionFactory() {
        // 打印当前配置信息以便调试
        log.debug("正在创建RabbitMQ连接: host={}, port={}", properties.getHost(), properties.getPort());
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(properties.getHost());
        factory.setPort(properties.getPort());
        factory.setUsername(properties.getUsername());
        factory.setPassword(properties.getPassword());

        // 测试连接是否可用
        try {
            com.rabbitmq.client.Connection connection = factory.newConnection();
            connection.close();
            log.debug("RabbitMQ连接测试成功: {}:{}", properties.getHost(), properties.getPort());
        } catch (Exception e) {
            log.error("RabbitMQ连接测试失败: {}:{}", properties.getHost(), properties.getPort(), e);
            throw new IllegalStateException("无法连接到RabbitMQ服务器: " + e.getMessage(), e);
        }

        return factory;
    }

    @Data
    @ConfigurationProperties(prefix = "spring.rabbitmq")
    public static class RabbitMQProperties {

        private String host;
        private int port ;
        private String username ;
        private String password;
        private String queueName ;
        private boolean durable ;
        private boolean exclusive ;
        private boolean autoDelete;


    }
    public RabbitMQProperties getProperties() {
        return properties;
    }
}