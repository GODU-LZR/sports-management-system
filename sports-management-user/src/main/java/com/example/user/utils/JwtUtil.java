package com.example.user.utils; // 注意包名可能不同

import com.example.common.dto.UserRoleWrapper;
import com.example.common.dto.UserRoleWrapper.RoleInfo;
// import com.example.gateway.util.RedisUtil; // user-service 不应该依赖 gateway 的 util
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger; // 使用 SLF4J
import org.slf4j.LoggerFactory; // 使用 SLF4J

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class); // 日志记录器


    @Value("${jwt.secret:4926644aA}") // 建议从配置中心或环境变量读取更安全的密钥
    private String secret;

    @Value("${jwt.expiration:3600}") // 单位：秒
    private long expiration; // JWT 本身的过期时间

    // --- 新增常量 ---
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USER_CODE = "userCode";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_STATUS = "status";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_FINGERPRINT = "clientFingerprint"; // 指纹的 Claim Key



    /**
     * 生成 JWT
     *
     * @param userRoleWrapper 包含用户信息的包装类，现在也包含客户端指纹
     * @return 生成的 JWT 字符串
     */
    public String generateToken(UserRoleWrapper userRoleWrapper) {
        log.info("开始生成用户 {} 的 JWT, 客户端指纹: {}", userRoleWrapper.getUserId(), userRoleWrapper.getClientFingerprint());
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_ID, userRoleWrapper.getUserId());
        claims.put(CLAIM_USER_CODE,userRoleWrapper.getUserCode());
        claims.put(CLAIM_USERNAME, userRoleWrapper.getUsername());
        claims.put(CLAIM_EMAIL, userRoleWrapper.getEmail());
        claims.put(CLAIM_STATUS, userRoleWrapper.getStatus());

        // 将角色列表转换为逗号分隔的角色编码字符串
        String rolesStr = userRoleWrapper.getRoles().stream()
                .map(RoleInfo::getRoleCode) // 使用 RoleInfo::getRoleCode 引用方法
                .collect(Collectors.joining(","));
        claims.put(CLAIM_ROLES, rolesStr);

        // === 新增：将客户端指纹添加到 Claims ===
        claims.put(CLAIM_FINGERPRINT, userRoleWrapper.getClientFingerprint());

        // 使用标准的 iat (Issued At) 和 exp (Expiration Time)
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000); // expiration 单位是秒

        String token = Jwts.builder()
                .setClaims(claims) // 设置自定义 claims
                .setIssuedAt(now) // 设置签发时间
                .setExpiration(expiryDate) // 设置过期时间
                .signWith(SignatureAlgorithm.HS512, secret) // 设置签名算法和密钥
                .compact();

        log.info("用户 {} 的 JWT 生成成功", userRoleWrapper.getUserId());

        return token;
    }

    /**
     * 解析 JWT, 获取 Claims
     *
     * @param token JWT 字符串
     * @return Claims 对象，如果解析失败或 token 无效则返回 null
     */
    public Claims parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret) // 设置签名密钥进行验证
                    .parseClaimsJws(token) // 解析 JWS（签名的 JWT）
                    .getBody(); // 获取 Payload 部分 (Claims)
            log.debug("JWT 解析成功: {}", claims);
            return claims;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("JWT 已过期: {}", token, e);
            return null;
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.error("不支持的 JWT 格式: {}", token, e);
            return null;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.error("JWT 格式错误: {}", token, e);
            return null;
        } catch (io.jsonwebtoken.SignatureException e) {
            log.error("JWT 签名无效: {}", token, e);
            return null;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims 字符串为空: {}", token, e);
            return null;
        } catch (Exception e) { // 捕获其他可能的异常
            log.error("JWT 解析时发生未知错误: {}", token, e);
            return null;
        }
    }

    /**
     * 检查 JWT 是否过期 (基于 Claims 中的 exp 字段)
     *
     * @param claims 从 parseToken 获取的 Claims 对象
     * @return true 如果已过期, false 如果未过期
     */
    public boolean isTokenExpired(Claims claims) {
        if (claims == null || claims.getExpiration() == null) {
            return true; // 无效 claims 或无过期时间视为过期
        }
        boolean expired = claims.getExpiration().before(new Date());
        if (expired) {
            log.debug("Token 已过期，过期时间: {}", claims.getExpiration());
        }
        return expired;
    }

    /**
     * 从 Claims 中提取 UserRoleWrapper 信息 (包含指纹)
     *
     * @param claims 从 parseToken 获取的 Claims 对象
     * @return 提取出的 UserRoleWrapper 对象，如果 claims 无效或缺少关键信息则返回 null
     */
    public UserRoleWrapper extractUserRoleWrapper(Claims claims) {
        if (claims == null) {
            log.warn("尝试从 null Claims 中提取 UserRoleWrapper");
            return null;
        }
        try {
            UserRoleWrapper userRoleWrapper = new UserRoleWrapper();
            // 使用常量 Key 获取 Claim，并指定类型
            userRoleWrapper.setUserId(claims.get(CLAIM_USER_ID, Long.class));
            userRoleWrapper.setUserCode(claims.get(CLAIM_USER_CODE, String.class));
            userRoleWrapper.setUsername(claims.get(CLAIM_USERNAME, String.class));
            userRoleWrapper.setEmail(claims.get(CLAIM_EMAIL, String.class));
            userRoleWrapper.setStatus(claims.get(CLAIM_STATUS, Integer.class));

            // === 新增：提取客户端指纹 ===
            userRoleWrapper.setClientFingerprint(claims.get(CLAIM_FINGERPRINT, String.class));

            // 将角色字符串拆分并转换为 List<RoleInfo>
            String rolesStr = claims.get(CLAIM_ROLES, String.class);
            if (rolesStr != null && !rolesStr.isEmpty()) {
                List<RoleInfo> rolesList = Arrays.stream(rolesStr.split(","))
                        // 注意：这里无法从 Claim 获取 roleId 和 roleName，只能获取 roleCode
                        // 如果下游需要完整的 RoleInfo，需要在网关或下游服务中重新查询
                        .map(roleCode -> new RoleInfo(null, null, roleCode)) // 仅填充 roleCode
                        .collect(Collectors.toList());
                userRoleWrapper.setRoles(rolesList);
            } else {
                userRoleWrapper.setRoles(List.of()); // 如果没有角色，设置为空列表
            }

            // 从标准 iat 和 exp 字段获取时间
            Date issuedAtDate = claims.getIssuedAt();
            Date expirationDate = claims.getExpiration();

            // 将 Date 转换为 LocalDateTime
            if (issuedAtDate != null) {
                userRoleWrapper.setIssuedAt(issuedAtDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
            if (expirationDate != null) {
                userRoleWrapper.setExpiration(expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }

            log.debug("从 Claims 成功提取 UserRoleWrapper: {}", userRoleWrapper);
            return userRoleWrapper;

        } catch (Exception e) {
            log.error("从 Claims 提取 UserRoleWrapper 时出错: {}", claims, e);
            return null;
        }
    }

    // --- 新增：单独获取 Claim 的方法，可能在网关中有用 ---

    /**
     * 从 Claims 中安全地获取指定 Key 的值
     * @param claims Claims 对象
     * @param key Claim 的 Key
     * @param requiredType 期望的类型 Class
     * @return Claim 的值，如果不存在或类型不匹配则返回 null
     */
    public <T> T getClaimFromToken(Claims claims, String key, Class<T> requiredType) {
        if (claims == null || key == null || requiredType == null) {
            return null;
        }
        try {
            return claims.get(key, requiredType);
        } catch (Exception e) {
            log.warn("从 Claims 获取 Key '{}' 的值时出错或类型不匹配: {}", key, e.getMessage());
            return null;
        }
    }
}
