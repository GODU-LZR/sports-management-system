package com.example.middleware.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// 如果添加到主类，则不需要 @Configuration
// 如果是新建配置类，则需要 @Configuration
@Configuration 
public class AppConfig { // 类名可以自定义，或者直接加到 MiddleWareApplication 类里面

    @Bean
    public RestTemplate restTemplate() {
        // 这里可以进行更复杂的配置，比如设置超时、添加拦截器等
        // 但最简单的就是直接 new 一个
        return new RestTemplate();
    }
}
