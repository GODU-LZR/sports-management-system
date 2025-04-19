package com.example.middleware;

import com.example.common.config.*;
import com.example.common.utils.RedisUtil;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = "com.example")
@EnableDiscoveryClient
@EnableDubbo
@Import({WebSecurityConfig.class, OpenAPIConfig.class, WebMvcConfig.class, JacksonConfig.class, RedisConfig.class, RedisUtil.class}) // 确保加载 common 模块的配置
public class MiddleWareApplication {
    public static void main(String[] args) {
        SpringApplication.run(MiddleWareApplication.class, args);
    }
}
