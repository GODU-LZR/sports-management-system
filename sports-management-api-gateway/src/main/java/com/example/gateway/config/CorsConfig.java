package com.example.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    @Primary
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有请求方法
        config.addAllowedMethod("*");

        // 获取所有允许的源
        List<String> origins = Arrays.asList(allowedOrigins.split(","));

        // 改用setAllowedOriginPatterns
        // 当allowCredentials为true时，Spring 5.3+推荐使用此方法
        config.setAllowedOriginPatterns(origins);

        // 允许携带cookie
        config.setAllowCredentials(true);

        // 添加需要公开的响应头
        config.addExposedHeader("Authorization");
        config.addExposedHeader("*");  // 公开所有头部

        // 设置预检请求的缓存时间（单位：秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
