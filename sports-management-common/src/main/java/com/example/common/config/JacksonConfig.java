package com.example.common.config;

import com.fasterxml.jackson.core.JsonFactory; // 导入 JsonFactory
import com.fasterxml.jackson.core.StreamReadConstraints; // 导入 StreamReadConstraints
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {

        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

        // --- START: 直接在 JsonFactory 上配置 StreamReadConstraints ---
        // 由于你使用的是 Spring Boot 2.7.x (或更早版本)，Jackson2ObjectMapperBuilder 没有 factoryConfigurer 方法。
        // 但你的 Jackson 版本 (2.15+) 支持 StreamReadConstraints，所以我们直接获取 ObjectMapper 的 JsonFactory 进行配置。

        int newMaxStringLength = 10_000_000; // 例如设置为 10MB (10,000,000)
        // int newMaxStringLength = 20_000_000; // 如果 10MB 不够，可以尝试 20MB

        StreamReadConstraints constraints = StreamReadConstraints.builder()
                .maxStringLength(newMaxStringLength)
                .build();

        JsonFactory factory = objectMapper.getFactory();
        if (factory != null) {
            factory.setStreamReadConstraints(constraints);
        }
        // --- END: 配置 StreamReadConstraints ---


        // 全局配置序列化返回 JSON 处理
        SimpleModule simpleModule = new SimpleModule();
        // JSON Long ==> String
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);

        // 注册 JavaTimeModule 以支持 LocalDateTime 的序列化/反序列化
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        objectMapper.registerModule(javaTimeModule);

        return objectMapper;
    }
}
