package com.example.common.enums;

/**
 * 应用程序角色枚举。
 * 这些常量源自数据库中的 'role_code' 字段，可用于 Spring Security 权限校验。
 */
public enum AppRole {
    /**
     * 超级管理员
     */
    SUPER_ADMIN,

    /**
     * 用户管理员
     */
    USER_ADMIN,

    /**
     * 人事管理员
     */
    HR_ADMIN,

    /**
     * 场地管理员
     */
    VENUE_ADMIN,

    /**
     * 器材管理员
     */
    EQUIPMENT_ADMIN,

    /**
     * 赛事管理员
     */
    EVENT_ADMIN,

    /**
     * 财务管理员
     */
    FINANCE_ADMIN,

    /**
     * 论坛新闻管理员
     */
    FORUM_ADMIN,

    /**
     * 普通用户
     */
    USER,

    /**
     * VIP用户
     */
    VIP_USER;

    /**
     * 获取此角色作为权限认证字符串（通常直接是 role_code）。
     * 例如: "SUPER_ADMIN"
     * 如果您的 GrantedAuthority 直接存储为 "SUPER_ADMIN", "USER_ADMIN" 等，
     * 可以在 @PreAuthorize("hasAuthority('SUPER_ADMIN')") 中使用。
     * 或者以编程方式引用: AppRole.SUPER_ADMIN.name()
     *
     * @return 角色代码字符串
     */
    public String getAuthority() {
        return this.name(); // 枚举常量的名称即为 role_code
    }

    /**
     * 获取带有 "ROLE_" 前缀的角色权限字符串。
     * 例如: "ROLE_SUPER_ADMIN"
     * 这是 Spring Security 中 hasRole() 表达式的典型用法，
     * 如果您的 GrantedAuthority 是以此种形式存储的（例如 "ROLE_SUPER_ADMIN" 对应于 hasRole("SUPER_ADMIN")）。
     *
     * @return 带 "ROLE_" 前缀的角色代码字符串
     */
    public String getRoleWithPrefix() {
        return "ROLE_" + this.name();
    }
}