package com.example.common.model;


import com.alibaba.fastjson2.annotation.JSONField;
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
    /**
     * 用户ID（唯一标识）
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 账号状态：0-正常，1-封禁15天，2-封禁30天，3-永久封禁
     */
    private Integer status;

    /**
     * 角色列表
     */
    private List<RoleInfo> roles;

    /**
     * JWT 签发时间
     */
    private LocalDateTime issuedAt;

    /**
     * JWT 过期时间
     */
    private LocalDateTime expiration;

    /**
     * 角色信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleInfo {
        /**
         * 角色ID（唯一标识）
         */
        private Long roleId;

        /**
         * 角色名称
         */
        private String roleName;

        /**
         * 角色编码（唯一）
         */
        private String roleCode;
    }
}
