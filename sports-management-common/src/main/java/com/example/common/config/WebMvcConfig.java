package com.example.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private UserConstantArgumentResolver userConstantArgumentResolver; // 注入我们自定义的解析器

    /**
     * 添加自定义的参数解析器到 Spring MVC 处理流程中
     * @param resolvers 参数解析器列表
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // 将我们的 UserRoleWrapperArgumentResolver 添加到解析器列表中
        resolvers.add(userConstantArgumentResolver);
        // 这里可以添加其他的自定义 ArgumentResolver
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH","DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}