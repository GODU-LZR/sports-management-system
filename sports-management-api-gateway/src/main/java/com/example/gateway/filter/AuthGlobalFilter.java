package com.example.gateway.filter;

import com.example.common.response.Result; // 导入 common 的 Result
import com.example.common.response.ResultCode; // 导入 common 的 ResultCode
import com.example.common.dto.UserRoleWrapper;
import com.example.common.utils.RedisUtil;
import com.example.gateway.util.JwtUtil; // 导入 gateway 的 JwtUtil
import com.fasterxml.jackson.core.JsonProcessingException; // Jackson 异常
import com.fasterxml.jackson.databind.ObjectMapper; // Jackson ObjectMapper
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException; // 导入特定异常
import io.jsonwebtoken.JwtException; // 导入 JWT 通用异常
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory; // 导入 DataBufferFactory
import org.springframework.data.redis.connection.RedisConnection; // 导入 RedisConnection
import org.springframework.data.redis.connection.RedisConnectionFactory; // 导入 RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate; // <<<--- 新增导入 RedisTemplate
import org.springframework.http.HttpHeaders; // 导入 HttpHeaders
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // 导入 MediaType
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException; // 导入 ResponseStatusException
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers; // 导入 Schedulers

import javax.annotation.PostConstruct; // 导入 PostConstruct
import java.nio.charset.StandardCharsets;
import java.time.Duration; // 导入 Duration
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects; // 导入 Objects
import java.util.concurrent.TimeoutException; // 导入 TimeoutException
import java.util.stream.Collectors;

@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {


    @Autowired
    private JwtUtil jwtUtil; // gateway 的 JwtUtil

    @Autowired(required = false) // 仍然注入 RedisUtil 用于 get 操作
    private RedisUtil redisUtil; // gateway 的 RedisUtil (阻塞式)

    @Autowired(required = false) // <<<--- 直接注入 RedisTemplate 用于 PING 检查
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false) // ObjectMapper 通常是必需的，用于错误响应序列化
    private ObjectMapper objectMapper; // 注入 ObjectMapper 用于序列化 Result 对象

    // Redis Key 前缀常量 (与 user-service 保持一致)
    private static final String REDIS_JWT_KEY_PREFIX = "jwt:user:";
    // 请求头常量
    private static final String HEADER_AUTHORIZATION = HttpHeaders.AUTHORIZATION;
    private static final String HEADER_FINGERPRINT = "X-Client-Fingerprint"; // 前端发送的指纹头
    private static final String BEARER_PREFIX = "Bearer ";

    // 透传给下游服务的用户信息头
    private static final String HEADER_X_USER_ID = "X-User-Id";
    private static final String HEADER_X_USER_USER_CODE = "X-User-Code";
    private static final String HEADER_X_USER_USERNAME = "X-User-Username";
    private static final String HEADER_X_USER_EMAIL = "X-User-Email";
    private static final String HEADER_X_USER_STATUS = "X-User-Status";
    private static final String HEADER_X_USER_ROLES = "X-User-Roles"; // 角色编码，逗号分隔

    // 白名单路径，不需要验证 token 和指纹
    private static final List<String> WHITE_LIST = new ArrayList<>();

    static {
        // ... 白名单列表保持不变 ...
        WHITE_LIST.add("/api/user/login");
        WHITE_LIST.add("/api/user/register");
        WHITE_LIST.add("/swagger-ui.html");
        WHITE_LIST.add("/swagger-ui/");
        WHITE_LIST.add("/swagger-ui/**");
        WHITE_LIST.add("/webjars/");
        WHITE_LIST.add("/webjars/**");
        WHITE_LIST.add("/v3/api-docs");
        WHITE_LIST.add("/v3/api-docs/");
        // 各微服务 Swagger 聚合接口 (保持不变)
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
        WHITE_LIST.add("/api/middleware/v3/api-docs");
        WHITE_LIST.add("/api/middleware/v3/api-docs/");
        WHITE_LIST.add("/api/ai/v3/api-docs");
        WHITE_LIST.add("/api/ai/v3/api-docs/");
    }

    @PostConstruct
    public void checkDependencies() {
        log.info("【AuthGlobalFilter】正在执行 PostConstruct 依赖检查...");
        boolean redisUtilOk = false;
        if (redisUtil == null) {
            log.error("【AuthGlobalFilter】致命错误: RedisUtil 未能注入! 核心认证功能 (get) 将失败!");
        } else {
            log.info("【AuthGlobalFilter】RedisUtil 已成功注入.");
            redisUtilOk = true; // 标记 RedisUtil 可用
        }

        boolean redisTemplateOk = false;
        if (redisTemplate == null) {
            log.error("【AuthGlobalFilter】错误: RedisTemplate 未能注入! 无法执行启动时的 PING 检查.");
        } else {
            log.info("【AuthGlobalFilter】RedisTemplate 已成功注入 (用于 PING 检查).");
            // <<<--- 使用注入的 redisTemplate 进行 PING 测试
            try {
                RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
                if (connectionFactory == null) {
                    log.error("【AuthGlobalFilter】错误: 从 RedisTemplate 获取 ConnectionFactory 失败!");
                } else {
                    // 使用 try-with-resources 确保连接被关闭
                    try (RedisConnection connection = connectionFactory.getConnection()) {
                        String pong = connection.ping();
                        log.info("【AuthGlobalFilter】初始化 Redis PING 测试成功: {}", pong);
                        redisTemplateOk = true; // PING 成功
                    } // 连接在此自动关闭
                }
            } catch (Exception e) {
                log.error("【AuthGlobalFilter】初始化 Redis PING 测试失败! 请检查 Redis 连接配置和状态。", e);
            }
        }

        // 综合日志
        if (!redisUtilOk && !redisTemplateOk) {
            log.error("【AuthGlobalFilter】RedisUtil 和 RedisTemplate 均未能成功初始化/连接! 认证功能严重受限!");
        } else if (!redisUtilOk) {
            log.warn("【AuthGlobalFilter】RedisUtil 未注入，但 RedisTemplate PING 成功。核心 get 操作将失败!");
        } else if (!redisTemplateOk && redisUtilOk) {
            log.warn("【AuthGlobalFilter】RedisUtil 已注入，但 RedisTemplate PING 失败或未注入。启动检查受限，但 get 操作可能仍有效。");
        } else {
            log.info("【AuthGlobalFilter】RedisUtil 和 RedisTemplate PING 检查均完成 (状态见上)。");
        }

        if (objectMapper == null) {
            log.warn("【AuthGlobalFilter】警告: ObjectMapper 未注入! 错误响应将使用纯文本格式。");
        } else {
            log.info("【AuthGlobalFilter】ObjectMapper 已成功注入.");
        }
        log.info("【AuthGlobalFilter】PostConstruct 依赖检查完成.");
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis(); // 记录开始时间
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpHeaders headers = request.getHeaders();
        String requestId = request.getId(); // 获取请求 ID 用于追踪

        log.info("【AuthGlobalFilter】[请求ID: {}] 开始处理请求: {}", requestId, path);

        // 0. 检查 RedisUtil 是否可用 (核心功能依赖)
        // <<<--- 这里的检查保持不变，因为 filter 逻辑依赖 redisUtil.get()
        if (redisUtil == null) {
            log.error("【AuthGlobalFilter】[请求ID: {}] 致命错误: RedisUtil 未注入，无法处理需要认证的请求!", requestId);
            return errorResponse(exchange, ResultCode.ERROR, "系统内部错误[R001]");
        }

        log.info("【AuthGlobalFilter】[请求ID: {}] Step 1: 检查路径是否在白名单中: {}", requestId, path);
        // 1. 白名单直接放行
        if (isWhitePath(path)) {
            log.info("【AuthGlobalFilter】[请求ID: {}] 路径在白名单中, 直接放行: {}", requestId, path);
            return chain.filter(exchange)
                    .doFinally(signalType -> {
                        long endTime = System.currentTimeMillis();
                        log.info("【AuthGlobalFilter】[请求ID: {}] 白名单请求处理完成, 信号: {}, 总耗时: {}ms", requestId, signalType, (endTime - startTime));
                    });
        }
        log.info("【AuthGlobalFilter】[请求ID: {}] Step 2: 路径不在白名单, 需要认证: {}", requestId, path);

        // ... [后续的 Header 获取、JWT 解析、Claims 提取逻辑保持不变] ...
        log.info("【AuthGlobalFilter】[请求ID: {}] Step 3: 获取 Authorization 和 Fingerprint Header...", requestId);
        String authHeader = headers.getFirst(HEADER_AUTHORIZATION);
        String requestFingerprint = headers.getFirst(HEADER_FINGERPRINT);

        log.info("【AuthGlobalFilter】[请求ID: {}] Step 4: 校验 Authorization Header...", requestId);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("【AuthGlobalFilter】[请求ID: {}] 请求缺少或格式错误的 Authorization Header", requestId);
            return errorResponse(exchange, ResultCode.UNAUTHORIZED, "缺少认证信息");
        }
        String token = authHeader.substring(BEARER_PREFIX.length());
        log.debug("【AuthGlobalFilter】[请求ID: {}] 获取到 Bearer Token (前缀已移除)", requestId);

        log.info("【AuthGlobalFilter】[请求ID: {}] Step 5: 校验 Fingerprint Header...", requestId);
        if (!StringUtils.hasText(requestFingerprint)) {
            log.warn("【AuthGlobalFilter】[请求ID: {}] 请求缺少 {} Header", requestId, HEADER_FINGERPRINT);
            return errorResponse(exchange, ResultCode.UNAUTHORIZED, "缺少客户端指纹信息");
        }
        log.debug("【AuthGlobalFilter】[请求ID: {}] 获取到客户端指纹: {}", requestId, requestFingerprint);

        log.info("【AuthGlobalFilter】[请求ID: {}] Step 6: 开始同步解析 JWT Token...", requestId);
        Claims claims;
        try {
            claims = jwtUtil.parseToken(token);
            if (claims == null) {
                log.warn("【AuthGlobalFilter】[请求ID: {}] Token 解析失败 (parseToken 返回 null)", requestId);
                return errorResponse(exchange, ResultCode.UNAUTHORIZED, "无效的认证信息");
            }
            log.info("【AuthGlobalFilter】[请求ID: {}] Token 解析成功", requestId);
        } catch (ExpiredJwtException eje) {
            log.warn("【AuthGlobalFilter】[请求ID: {}] Token 已过期 (解析时发现): {}", requestId, eje.getMessage());
            return errorResponse(exchange, ResultCode.UNAUTHORIZED, "认证信息已过期，请重新登录");
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("【AuthGlobalFilter】[请求ID: {}] Token 无效 (解析时发现): {}", requestId, e.getMessage());
            return errorResponse(exchange, ResultCode.UNAUTHORIZED, "无效的认证信息");
        } catch (Exception e) {
            log.error("【AuthGlobalFilter】[请求ID: {}] 解析 Token 时发生未知错误", requestId, e);
            return errorResponse(exchange, ResultCode.ERROR, "认证处理失败[JWT001]");
        }

        log.info("【AuthGlobalFilter】[请求ID: {}] Step 7: 双重检查 Token 是否过期...", requestId);
        if (jwtUtil.isTokenExpired(claims)) {
            log.warn("【AuthGlobalFilter】[请求ID: {}] Token 已过期 (双重检查), 过期时间: {}", requestId, claims.getExpiration());
            return errorResponse(exchange, ResultCode.UNAUTHORIZED, "认证信息已过期，请重新登录");
        }
        log.debug("【AuthGlobalFilter】[请求ID: {}] Token 未过期 (双重检查通过)", requestId);

        log.info("【AuthGlobalFilter】[请求ID: {}] Step 8: 从 Claims 提取 userId 和指纹...", requestId);
        Long userId;
        String tokenFingerprint;
        UserRoleWrapper userRoleWrapper;
        try {
            userId = jwtUtil.getClaimFromToken(claims, JwtUtil.CLAIM_USER_ID, Long.class);
            tokenFingerprint = jwtUtil.getClaimFromToken(claims, JwtUtil.CLAIM_FINGERPRINT, String.class);
            userRoleWrapper = jwtUtil.extractUserRoleWrapper(claims);

            if (userId == null || !StringUtils.hasText(tokenFingerprint)) {
                log.error("【AuthGlobalFilter】[请求ID: {}] Token Claims 中缺少 userId 或 clientFingerprint", requestId);
                return errorResponse(exchange, ResultCode.UNAUTHORIZED, "认证信息不完整[C001]");
            }
            if (userRoleWrapper == null) {
                log.error("【AuthGlobalFilter】[请求ID: {}] Token Claims 中无法提取用户信息 (UserRoleWrapper 为 null)", requestId);
                return errorResponse(exchange, ResultCode.UNAUTHORIZED, "认证信息不完整[C002]");
            }
            log.info("【AuthGlobalFilter】[请求ID: {}] 从 Claims 成功提取 userId: {}, Token指纹: {}, 用户信息已提取", requestId, userId, tokenFingerprint);
        } catch (Exception e) {
            log.error("【AuthGlobalFilter】[请求ID: {}] 从 Claims 提取信息时发生异常", requestId, e);
            return errorResponse(exchange, ResultCode.ERROR, "处理认证信息时出错[C003]");
        }

        // 8. === 核心校验：Redis Token 比对 (单点登录) 和 指纹比对 ===
        String redisKey = REDIS_JWT_KEY_PREFIX + userId;
        log.info("【AuthGlobalFilter】[请求ID: {}] Step 9: 准备访问 Redis 进行 Token 和指纹校验, Key: {}", requestId, redisKey);

        // <<<--- 这里的 Redis 调用仍然使用 redisUtil.get()，保持不变
        return Mono.fromCallable(() -> {
                    long redisCallStartTime = System.currentTimeMillis();
                    log.info("【AuthGlobalFilter】[线程: {}] [请求ID: {}] 开始执行 Redis GET 操作 (阻塞调用), Key: {}", Thread.currentThread().getName(), requestId, redisKey);
                    try {
                        // *** 核心 Redis 调用 ***
                        Object result = redisUtil.get(redisKey); // 这是阻塞操作
                        long redisCallEndTime = System.currentTimeMillis();
                        log.info("【AuthGlobalFilter】[线程: {}] [请求ID: {}] Redis GET 操作完成, 耗时: {}ms, 结果: {}, Key: {}",
                                Thread.currentThread().getName(), requestId, (redisCallEndTime - redisCallStartTime), (result != null ? "非空" : "空"), redisKey);
                        return result; // 返回 Redis 查询结果
                    } catch (Exception e) {
                        long redisCallEndTime = System.currentTimeMillis();
                        log.error("【AuthGlobalFilter】[线程: {}] [请求ID: {}] Redis GET 操作异常! 耗时: {}ms, Key: {}, 错误: {}",
                                Thread.currentThread().getName(), requestId, (redisCallEndTime - redisCallStartTime), redisKey, e.getMessage(), e);
                        throw new RuntimeException("Redis 操作失败: " + e.getMessage(), e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSubscribe(subscription -> log.info("【AuthGlobalFilter】[请求ID: {}] Redis Mono 已订阅 (准备切换到 BoundedElastic 线程)", requestId))
                .timeout(Duration.ofSeconds(5), Mono.error(new TimeoutException("Redis GET 操作超时(5秒), Key: " + redisKey)))
                .doOnError(error -> log.error("【AuthGlobalFilter】[请求ID: {}] Redis Mono 遇到错误 (可能是超时或执行异常), Key: {}, 错误类型: {}, 错误: {}", requestId, redisKey, error.getClass().getSimpleName(), error.getMessage()))
                .flatMap(cachedTokenObj -> {
                    log.info("【AuthGlobalFilter】[线程: {}] [请求ID: {}] 进入 flatMap 处理 Redis 结果", Thread.currentThread().getName(), requestId);

                    log.info("【AuthGlobalFilter】[请求ID: {}] Step 9.1: 检查 Redis 结果是否为 null", requestId);
                    if (cachedTokenObj == null) {
                        log.warn("【AuthGlobalFilter】[请求ID: {}] 用户 {} 的 Token 不在 Redis 中 (已登出或过期), Key: {}", requestId, userId, redisKey);
                        return errorResponse(exchange, ResultCode.UNAUTHORIZED, "认证信息已失效，请重新登录");
                    }
                    String cachedToken = String.valueOf(cachedTokenObj);
                    log.debug("【AuthGlobalFilter】[请求ID: {}] Redis 中存在 Token", requestId);

                    log.info("【AuthGlobalFilter】[请求ID: {}] Step 9.2: 比对请求 Token 与 Redis Token", requestId);
                    if (!Objects.equals(token, cachedToken)) {
                        log.warn("【AuthGlobalFilter】[请求ID: {}] 用户 {} 请求 Token 与 Redis Token 不匹配 (已在别处登录?)", requestId, userId);
                        return errorResponse(exchange, ResultCode.UNAUTHORIZED, "您已在其他地方登录，请重新登录");
                    }
                    log.debug("【AuthGlobalFilter】[请求ID: {}] Redis Token 校验通过", requestId);

                    log.info("【AuthGlobalFilter】[请求ID: {}] Step 9.3: 比对请求指纹与 Token 指纹", requestId);
                    if (!Objects.equals(requestFingerprint, tokenFingerprint)) {
                        log.warn("【AuthGlobalFilter】[请求ID: {}] 用户 {} 客户端指纹不匹配! 请求指纹: {}, Token指纹: {}", requestId, userId, requestFingerprint, tokenFingerprint);
                        return errorResponse(exchange, ResultCode.FORBIDDEN, "客户端环境已改变，请重新登录");
                    }
                    log.debug("【AuthGlobalFilter】[请求ID: {}] 客户端指纹校验通过", requestId);

                    log.info("【AuthGlobalFilter】[请求ID: {}] Step 10: 所有校验通过, 准备构造下游请求", requestId);
                    ServerHttpRequest newRequest = buildForwardRequest(request, userRoleWrapper);
                    ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();

                    log.info("【AuthGlobalFilter】[请求ID: {}] 准备调用 chain.filter() 放行请求到下游: {}", requestId, path);
                    return chain.filter(newExchange)
                            .doOnSuccess(v -> log.info("【AuthGlobalFilter】[请求ID: {}] 下游服务调用成功返回, Path: {}", requestId, path))
                            .doOnError(downstreamError -> log.error("【AuthGlobalFilter】[请求ID: {}] 下游服务调用失败! Path: {}, 错误: {}", requestId, path, downstreamError.getMessage(), downstreamError));
                })
                .onErrorResume(error -> {
                    log.error("【AuthGlobalFilter】[请求ID: {}] 认证流程中捕获到错误! Key: {}, 错误类型: {}, 错误信息: {}",
                            requestId, redisKey, error.getClass().getSimpleName(), error.getMessage(), error);

                    if (error instanceof ResponseStatusException) {
                        log.debug("【AuthGlobalFilter】[请求ID: {}] 检测到 ResponseStatusException，可能是由 errorResponse 触发，直接透传", requestId);
                        return Mono.error(error);
                    }
                    else if (error instanceof TimeoutException) {
                        return errorResponse(exchange, ResultCode.ERROR, "认证服务超时，请稍后重试[T001]");
                    } else if (error instanceof RuntimeException && error.getMessage() != null && error.getMessage().contains("Redis 操作失败")) {
                        return errorResponse(exchange, ResultCode.ERROR, "认证服务暂时不可用[R002]");
                    }
                    return errorResponse(exchange, ResultCode.ERROR, "系统内部错误[G001]");
                })
                .doFinally(signalType -> {
                    long endTime = System.currentTimeMillis();
                    log.info("【AuthGlobalFilter】[请求ID: {}] Filter 处理流程结束 (非白名单), 信号: {}, 总耗时: {}ms", requestId, signalType, (endTime - startTime));
                });
    }

    /**
     * 判断路径是否在白名单中 (保持不变)
     */
    private boolean isWhitePath(String path) {
        return WHITE_LIST.stream().anyMatch(whitePath -> path.startsWith(whitePath));
    }

    /**
     * 构建转发给下游服务的请求 (添加用户信息头) (保持不变)
     */
    private ServerHttpRequest buildForwardRequest(ServerHttpRequest originalRequest, UserRoleWrapper userRoleWrapper) {
        String requestId = originalRequest.getId();
        log.info("【AuthGlobalFilter】[请求ID: {}] Step 10.1: 开始构建转发给下游的请求...", requestId);

        // 示例 Basic Auth (保持不变)
        String systemUsername = "gatewayuser";
        String systemPassword = "gatewaypass";
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString((systemUsername + ":" + systemPassword)
                .getBytes(StandardCharsets.UTF_8));

        ServerHttpRequest modifiedRequest = originalRequest.mutate()
                .headers(httpHeaders -> {
                    log.debug("【AuthGlobalFilter】[请求ID: {}] 开始修改 Headers...", requestId);
                    httpHeaders.remove(HEADER_AUTHORIZATION);
                    httpHeaders.remove(HEADER_FINGERPRINT);
                    log.debug("【AuthGlobalFilter】[请求ID: {}] 移除了原始 Authorization 和 Fingerprint Header", requestId);

                    httpHeaders.set(HttpHeaders.AUTHORIZATION, basicAuth);
                    log.debug("【AuthGlobalFilter】[请求ID: {}] 添加了内部调用 Basic Auth Header", requestId);

                    httpHeaders.set(HEADER_X_USER_ID, String.valueOf(userRoleWrapper.getUserId()));
                    if (StringUtils.hasText(userRoleWrapper.getUserCode())) {
                        httpHeaders.set(HEADER_X_USER_USER_CODE, userRoleWrapper.getUserCode());
                    }
                    if (StringUtils.hasText(userRoleWrapper.getUsername())) {
                        httpHeaders.set(HEADER_X_USER_USERNAME, userRoleWrapper.getUsername());
                    }
                    if (StringUtils.hasText(userRoleWrapper.getEmail())) {
                        httpHeaders.set(HEADER_X_USER_EMAIL, userRoleWrapper.getEmail());
                    }
                    httpHeaders.set(HEADER_X_USER_STATUS, String.valueOf(userRoleWrapper.getStatus()));

                    String rolesStr = userRoleWrapper.getRoles().stream()
                            .map(UserRoleWrapper.RoleInfo::getRoleCode)
                            .filter(StringUtils::hasText)
                            .collect(Collectors.joining(","));
                    if (StringUtils.hasText(rolesStr)) {
                        httpHeaders.set(HEADER_X_USER_ROLES, rolesStr);
                    }
                    log.info("【AuthGlobalFilter】[请求ID: {}] 添加了下游用户信息 Headers: UserId={}, UserCode={},Username={}, Email={}, Status={}, Roles={}",
                            requestId,
                            userRoleWrapper.getUserId(),
                            userRoleWrapper.getUserCode(),
                            userRoleWrapper.getUsername() != null ? userRoleWrapper.getUsername() : "N/A",
                            userRoleWrapper.getEmail() != null ? userRoleWrapper.getEmail() : "N/A",
                            userRoleWrapper.getStatus(),
                            StringUtils.hasText(rolesStr) ? rolesStr : "N/A");
                })
                .build();
        log.info("【AuthGlobalFilter】[请求ID: {}] Step 10.2: 下游请求构建完成.", requestId);
        return modifiedRequest;
    }


    /**
     * 生成统一格式的错误响应 (JSON) (保持不变)
     */
    private Mono<Void> errorResponse(ServerWebExchange exchange, ResultCode resultCode, String message) {
        String requestId = exchange.getRequest().getId();
        String finalMessage = StringUtils.hasText(message) ? message : resultCode.getMessage();
        log.warn("【AuthGlobalFilter】[请求ID: {}] 认证/鉴权失败或处理错误, 准备返回错误响应: Code={}, Message={}", requestId, resultCode.getCode(), finalMessage);

        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            log.warn("【AuthGlobalFilter】[请求ID: {}] 响应已提交, 无法发送错误信息: Code={}, Message={}", requestId, resultCode.getCode(), finalMessage);
            // 对于已提交的响应，只能记录日志或抛出异常让上层处理
            return Mono.empty(); // 或者根据需要返回 Mono.error
        }

        response.setStatusCode(HttpStatus.valueOf(resultCode.getCode()));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> result = Result.error(resultCode.getCode(), finalMessage);

        // 处理 ObjectMapper 为 null 的情况 (保持不变)
        if (objectMapper == null) {
            log.error("【AuthGlobalFilter】[请求ID: {}] ObjectMapper 未注入，无法序列化错误响应! 返回纯文本错误。", requestId);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON); // 仍然尝试设为 JSON
            byte[] errorBytes = ("{\"code\":" + ResultCode.ERROR.getCode() + ", \"message\":\"服务内部错误[JSON001]\", \"success\": false, \"data\": null}").getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(errorBytes);
            // 直接返回写入操作的 Mono<Void>
            return response.writeWith(Mono.just(buffer));
        }

        try {
            byte[] responseBody = objectMapper.writeValueAsBytes(result);
            DataBufferFactory bufferFactory = response.bufferFactory();
            DataBuffer buffer = bufferFactory.wrap(responseBody);
            log.info("【AuthGlobalFilter】[请求ID: {}] 正在发送错误响应 (状态码: {})...", requestId, response.getStatusCode());
            // 直接返回写入操作的 Mono<Void>，它会在写入完成后发出 onComplete 信号，终止链
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("【AuthGlobalFilter】[请求ID: {}] 序列化错误响应失败: {}", requestId, result, e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON); // 仍然尝试设为 JSON
            byte[] errorBytes = ("{\"code\":" + ResultCode.ERROR.getCode() + ", \"message\":\"服务内部错误[JSON002]\", \"success\": false, \"data\": null}").getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(errorBytes);
            // 直接返回写入操作的 Mono<Void>
            return response.writeWith(Mono.just(buffer));
        }
    }



    @Override
    public int getOrder() {
        return -100; // 保持不变
    }
}
