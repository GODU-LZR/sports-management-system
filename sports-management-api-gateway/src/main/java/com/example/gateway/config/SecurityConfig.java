package com.example.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.util.StringUtils;
import org.springframework.web.server.WebFilter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf().disable()
            .authorizeExchange()
            .pathMatchers("/api/user/login", "/api/user/register").permitAll()
            // 允许所有 OPTIONS 请求
            .pathMatchers(HttpMethod.OPTIONS).permitAll()
            .anyExchange().permitAll()
            .and()
            .build();
    }

    @Bean
    public WebFilter securityContextFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            String userId = request.getHeaders().getFirst("X-User-Id");
            String username = request.getHeaders().getFirst("X-User-Username");
            String roles = request.getHeaders().getFirst("X-User-Roles");
            
            if (StringUtils.hasText(username) && StringUtils.hasText(roles)) {
                List<GrantedAuthority> authorities = Arrays.stream(roles.split(","))
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
                    
                Authentication auth = new UsernamePasswordAuthenticationToken(
                    username, null, authorities);
                    
                return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
            }
            
            return chain.filter(exchange);
        };
    }
}
