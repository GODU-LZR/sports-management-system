package com.example.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户角色信息包装类，用于配合 JWT 和 Spring Security 做权限校验
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleWrapper {
    // ... (原有字段 userId, username, email, status, roles, issuedAt, expiration) ...
    private Long userId;
    private String username;
    private String email;
    private Integer status;
    private List<RoleInfo> roles;
    private LocalDateTime issuedAt;
    private LocalDateTime expiration;

    /**
     * 客户端指纹 (登录时计算的 User-Agent SHA-256 哈希)
     * 新增字段
     */
    private String clientFingerprint;

    /**
     * 角色信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleInfo {
        private Long roleId;
        private String roleName;
        private String roleCode;
    }
}
