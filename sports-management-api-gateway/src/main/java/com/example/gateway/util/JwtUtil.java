package com.example.gateway.util; // 网关的包名

import com.example.common.dto.UserRoleWrapper; // 依赖 common 模块
import com.example.common.dto.UserRoleWrapper.RoleInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils; // 引入 StringUtils

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret:4926644aA}") // 确保与 user-service 一致
    private String secret;

    // --- Claim Keys (与 user-service 保持一致) ---
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USER_CODE = "userCode";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_STATUS = "status";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_FINGERPRINT = "clientFingerprint";

    /**
     * 解析 JWT, 获取 Claims
     * (与 user-service 的 parseToken 方法相同)
     */
    public Claims parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
            log.debug("网关 JWT 解析成功");
            return claims;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("网关检测到 JWT 已过期: {}", token); // 只记录警告，具体处理由 Filter 决定
            // 返回过期的 Claims，让 Filter 判断
            return e.getClaims();
        } catch (Exception e) { // 捕获其他解析错误
            log.error("网关 JWT 解析失败: {}", token, e);
            return null; // 解析彻底失败返回 null
        }
    }

    /**
     * 检查 JWT 是否过期 (基于 Claims 中的 exp 字段)
     * (与 user-service 的 isTokenExpired 方法相同)
     */
    public boolean isTokenExpired(Claims claims) {
        if (claims == null || claims.getExpiration() == null) {
            return true;
        }
        return claims.getExpiration().before(new Date());
    }

    /**
     * 从 Claims 中安全地获取指定 Key 的值
     * (与 user-service 的 getClaimFromToken 方法相同)
     */
    public <T> T getClaimFromToken(Claims claims, String key, Class<T> requiredType) {
        if (claims == null || key == null || requiredType == null) {
            return null;
        }
        try {
            return claims.get(key, requiredType);
        } catch (Exception e) {
            log.warn("网关从 Claims 获取 Key '{}' 的值时出错或类型不匹配: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 从 Claims 中提取 UserRoleWrapper 信息 (供下游服务使用)
     * 注意：这里的 RoleInfo 只包含 roleCode
     */
    public UserRoleWrapper extractUserRoleWrapper(Claims claims) {
        if (claims == null) {
            log.warn("网关尝试从 null Claims 中提取 UserRoleWrapper");
            return null;
        }
        try {
            UserRoleWrapper userRoleWrapper = new UserRoleWrapper();
            userRoleWrapper.setUserId(getClaimFromToken(claims, CLAIM_USER_ID, Long.class));
            userRoleWrapper.setUserCode(claims.get(CLAIM_USER_CODE, String.class));
            userRoleWrapper.setUsername(getClaimFromToken(claims, CLAIM_USERNAME, String.class));
            userRoleWrapper.setEmail(getClaimFromToken(claims, CLAIM_EMAIL, String.class));
            userRoleWrapper.setStatus(getClaimFromToken(claims, CLAIM_STATUS, Integer.class));


            String rolesStr = getClaimFromToken(claims, CLAIM_ROLES, String.class);
            if (StringUtils.hasText(rolesStr)) {
                List<RoleInfo> rolesList = Arrays.stream(rolesStr.split(","))
                        .map(roleCode -> new RoleInfo(null, null, roleCode))
                        .collect(Collectors.toList());
                userRoleWrapper.setRoles(rolesList);
            } else {
                userRoleWrapper.setRoles(List.of());
            }

            log.debug("网关从 Claims 成功提取 UserRoleWrapper (用于下游): userId={}", userRoleWrapper.getUserId());
            return userRoleWrapper;

        } catch (Exception e) {
            log.error("网关从 Claims 提取 UserRoleWrapper 时出错: {}", claims, e);
            return null;
        }
    }
}
