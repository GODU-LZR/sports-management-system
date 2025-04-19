package com.example.event.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;

/**
 * 赛事管理模块配置类
 */
@Configuration
@EnableWebMvc
public class EventControllerConfig implements WebMvcConfigurer {

    /**
     * 配置Swagger文档信息
     */
    @Bean
    public OpenAPI eventOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("赛事管理API")
                        .description("体育赛事管理系统的赛事管理模块API文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Sports Management System")
                                .email("contact@sportsmanagement.com")));
    }
}