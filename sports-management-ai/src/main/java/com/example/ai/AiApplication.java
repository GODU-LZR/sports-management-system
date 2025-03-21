package com.example.ai;

import com.example.common.config.JacksonConfig;
import com.example.common.config.OpenAPIConfig;
import com.example.common.config.WebMvcConfig;
import com.example.common.config.WebSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = "com.example")
@EnableDiscoveryClient
@Import({WebSecurityConfig.class, OpenAPIConfig.class, WebMvcConfig.class, JacksonConfig.class}) // 确保加载 common 模块的配置
public class AiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
    }
}
