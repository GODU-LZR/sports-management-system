package com.example.gateway.util;

import com.example.common.model.UserRoleWrapper;
import com.example.common.model.UserRoleWrapper.RoleInfo;
import com.example.gateway.util.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Autowired
    private RedisUtil redisUtil;

    @Value("${jwt.secret:4926644aA}")
    private String secret;

    @Value("${jwt.expiration:3600}")
    private long expiration;

    // 生成 JWT
    public String generateToken(UserRoleWrapper userRoleWrapper) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userRoleWrapper.getUserId());
        claims.put("username", userRoleWrapper.getUsername());
        claims.put("email", userRoleWrapper.getEmail());
        claims.put("status", userRoleWrapper.getStatus());

        // 将角色列表转换为逗号分隔的字符串（这里使用角色编码，可根据需要调整为角色名称）
        String rolesStr = userRoleWrapper.getRoles().stream()
                .map(RoleInfo::getRoleCode)
                .collect(Collectors.joining(","));
        claims.put("roles", rolesStr);

        // 格式化 LocalDateTime 为字符串（ISO_LOCAL_DATE_TIME 格式）
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        claims.put("issuedAt", userRoleWrapper.getIssuedAt().format(formatter));
        claims.put("expiration", userRoleWrapper.getExpiration().format(formatter));

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();

        // 将 token 存入 Redis
        redisUtil.set("JWT:" + userRoleWrapper.getUserId(), token, expiration, TimeUnit.SECONDS);

        return token;
    }

    // 解析 JWT
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    // 检查 JWT 是否过期
    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    // 从 Claims 中提取 UserRoleWrapper 信息
    public UserRoleWrapper extractUserRoleWrapper(Claims claims) {
        UserRoleWrapper userRoleWrapper = new UserRoleWrapper();
        userRoleWrapper.setUserId(claims.get("userId", Long.class));
        userRoleWrapper.setUsername(claims.get("username", String.class));
        userRoleWrapper.setEmail(claims.get("email", String.class));
        userRoleWrapper.setStatus(claims.get("status", Integer.class));

        // 将角色字符串拆分并转换为 List<RoleInfo>
        String rolesStr = claims.get("roles", String.class);
        List<RoleInfo> rolesList = Arrays.stream(rolesStr.split(","))
                .map(role -> new RoleInfo(null, role, role))
                .collect(Collectors.toList());
        userRoleWrapper.setRoles(rolesList);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        userRoleWrapper.setIssuedAt(LocalDateTime.parse(claims.get("issuedAt", String.class), formatter));
        userRoleWrapper.setExpiration(LocalDateTime.parse(claims.get("expiration", String.class), formatter));
        return userRoleWrapper;
    }
}
