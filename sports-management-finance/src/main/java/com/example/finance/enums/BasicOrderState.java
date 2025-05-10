package com.example.finance.enums;

public enum BasicOrderState {
    // 基础核心状态
    CREATED(1, "订单已创建"),
    PAID(2, "已支付"),
    PROCESSING(3, "处理中"),
    SHIPPED(4, "已发货"),
    DELIVERED(5, "已送达"),
    COMPLETED(10, "已完成"),

    // 取消相关状态
    CANCELLED_BY_USER(20, "用户取消"),
    CANCELLED_BY_SYSTEM(21, "系统自动取消"),

    // 逆向流程状态
    REFUND_REQUESTED(30, "退款申请中"),
    REFUNDED(31, "已退款"),
    RETURN_REQUESTED(32, "退货申请中"),
    RETURNED(33, "已退货");

    private final int code;
    private final String description;

    BasicOrderState(int code, String description) {
        this.code = code;
        this.description = description;
    }

    // Getter 方法
    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    // 根据code获取枚举
    public static BasicOrderState getByCode(int code) {
        for (BasicOrderState state : values()) {
            if (state.code == code) {
                return state;
            }
        }
        throw new IllegalArgumentException("Invalid order state code: " + code);
    }
}