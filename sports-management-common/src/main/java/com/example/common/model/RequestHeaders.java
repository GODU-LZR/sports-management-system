package com.example.common.model;

/**
 * 定义网关向下游服务传递用户信息的请求头常量
 * 这些常量应该在网关和所有下游服务中共享使用
 */
public final class RequestHeaders {

    private RequestHeaders() {
        // 私有构造函数，防止实例化
    }

    /**
     * 用户 ID 头
     */
    public static final String HEADER_X_USER_ID = "X-User-Id";
    /**
     * 用户名头
     */
    public static final String HEADER_X_USER_USERNAME = "X-User-Username";
    /**
     * 用户邮箱头
     */
    public static final String HEADER_X_USER_EMAIL = "X-User-Email";
    /**
     * 用户状态头
     */
    public static final String HEADER_X_USER_STATUS = "X-User-Status";
    /**
     * 用户角色编码头 (逗号分隔)
     */
    public static final String HEADER_X_USER_ROLES = "X-User-Roles";

}
