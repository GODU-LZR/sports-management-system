package com.example.gateway.filter;

import com.example.common.model.ResultCode;
import com.example.common.model.UserRoleWrapper;
import com.example.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Autowired(required = false)
    private JwtUtil jwtUtil;

    // 白名单路径，不需要验证 token
    private static final List<String> WHITE_LIST = new ArrayList<>();

    static {
        // 登录、注册以及 Swagger 相关接口放行
        WHITE_LIST.add("/api/user/login");
        WHITE_LIST.add("/api/user/register");
        WHITE_LIST.add("/swagger-ui.html");
        WHITE_LIST.add("/swagger-ui/");
        WHITE_LIST.add("/swagger-ui/**");
        WHITE_LIST.add("/webjars/");
        WHITE_LIST.add("/webjars/**");
        WHITE_LIST.add("/v3/api-docs");
        WHITE_LIST.add("/v3/api-docs/");
        // 各微服务 Swagger 聚合接口
        WHITE_LIST.add("/api/user/v3/api-docs");
        WHITE_LIST.add("/api/user/v3/api-docs/");
        WHITE_LIST.add("/api/venue/v3/api-docs");
        WHITE_LIST.add("/api/venue/v3/api-docs/");
        WHITE_LIST.add("/api/equipment/v3/api-docs");
        WHITE_LIST.add("/api/equipment/v3/api-docs/");
        WHITE_LIST.add("/api/event/v3/api-docs");
        WHITE_LIST.add("/api/event/v3/api-docs/");
        WHITE_LIST.add("/api/finance/v3/api-docs");
        WHITE_LIST.add("/api/finance/v3/api-docs/");
        WHITE_LIST.add("/api/forum/v3/api-docs");
        WHITE_LIST.add("/api/forum/v3/api-docs/");
        WHITE_LIST.add("/api/notification/v3/api-docs");
        WHITE_LIST.add("/api/notification/v3/api-docs/");
        WHITE_LIST.add("/api/ai/v3/api-docs");
        WHITE_LIST.add("/api/ai/v3/api-docs/");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单直接放行
        if (isWhitePath(path)) {
            return chain.filter(exchange);
        }
        System.out.println("被调用");

        // 获取客户端传入的 Bearer token 并校验
        String token = request.getHeaders().getFirst("Authorization");
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }
        System.out.println("获取token完成；" + token);

        token = token.substring(7); // 去掉 "Bearer " 前缀
        Claims claims = jwtUtil.parseToken(token);
        if (claims == null || jwtUtil.isTokenExpired(claims)) {
            return unauthorized(exchange);
        }
        System.out.println("解析token完成；" + claims.toString());

        UserRoleWrapper userRoleWrapper = jwtUtil.extractUserRoleWrapper(claims);
        if (userRoleWrapper == null) {
            return unauthorized(exchange);
        }
        System.out.println("提取token信息完成；" + userRoleWrapper.toString());

        // 固定系统账号，保证网关和下游之间通信安全
        String systemUsername = "gatewayuser";
        String systemPassword = "gatewaypass";
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString((systemUsername + ":" + systemPassword)
                .getBytes(StandardCharsets.UTF_8));

        // 构造新的请求，将原有 Authorization 头替换为固定 Basic Auth，同时传递自定义用户信息
        // 将 List<RoleInfo> 转换为逗号分隔的角色编码字符串
        String rolesStr = userRoleWrapper.getRoles().stream()
                .map(role -> role.getRoleCode())
                .collect(Collectors.joining(","));

        ServerHttpRequest newRequest = request.mutate()
                .header("Authorization", basicAuth) // 固定 Basic 认证头
                .header("X-User-Id", userRoleWrapper.getUserId().toString())
                .header("X-User-Username", userRoleWrapper.getUsername())
                .header("X-User-Email", userRoleWrapper.getEmail())
                .header("X-User-Status", userRoleWrapper.getStatus().toString())
                .header("X-User-Roles", rolesStr) // 这里使用正确格式的角色字符串
                .build();

        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();

        System.out.println("成功");
        return chain.filter(newExchange);
    }

    private boolean isWhitePath(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String result = "{\"code\":" + ResultCode.UNAUTHORIZED.getCode() +
                ",\"message\":\"" + ResultCode.UNAUTHORIZED.getMessage() + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(result.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Flux.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 确保此过滤器优先级较高
    }
}
