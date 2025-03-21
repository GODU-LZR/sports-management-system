package com.example.gateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SecurityContextConfig implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 从请求头获取用户信息
        String userId = request.getHeaders().getFirst("X-User-Id");
        String username = request.getHeaders().getFirst("X-User-Username");
        String roles = request.getHeaders().getFirst("X-User-Roles");
        
        if (StringUtils.hasText(username) && StringUtils.hasText(roles)) {
            // 构建用户认证信息
            List<GrantedAuthority> authorities = Arrays.stream(roles.split(","))
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
                
            Authentication auth = new UsernamePasswordAuthenticationToken(
                username, null, authorities);
                
            return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
        }
        
        return chain.filter(exchange);
    }
}
