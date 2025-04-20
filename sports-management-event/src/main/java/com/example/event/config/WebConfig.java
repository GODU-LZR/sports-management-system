package com.example.event.config;

import com.example.event.Interceptor.MatchIdValidationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private MatchIdValidationInterceptor matchIdValidationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(matchIdValidationInterceptor)
                .addPathPatterns("/api/**/match/**"); // 只拦截 match 路径
    }
}
