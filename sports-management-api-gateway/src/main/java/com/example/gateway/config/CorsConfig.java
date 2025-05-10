package com.example.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
public class CorsConfig {

    @Bean
    @Primary
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有请求方法
        config.addAllowedMethod("*");

        // 使用通配符模式允许所有源 - 这是关键修改
        config.setAllowedOriginPatterns(Collections.singletonList("*"));

        // 允许携带cookie
        config.setAllowCredentials(true);

        // 添加需要公开的响应头
        config.addExposedHeader("Authorization");

        // 设置预检请求的缓存时间（单位：秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
