package com.example.finance.enums;

import lombok.Getter;

@Getter
public enum OrderAmountStatus {
    // --- 支付相关 ---
    PENDING_PAYMENT(10, "待支付"),         // 订单已创建，等待用户支付
    PAYMENT_INITIATED(20, "支付已发起"),    // 用户点击支付按钮后
    PAYMENT_PROCESSING(30, "支付处理中"),    // 支付正在处理中
    PAYMENT_FAILED(40, "支付失败"),        // 支付尝试失败
    PARTIALLY_PAID(50, "部分支付"),        // 用户支付了部分金额 (如果你的业务支持)
    PAID(60, "已支付"),                 // 用户已全额支付
    PAYMENT_CONFIRMED(70, "支付已确认"),    // 支付平台返回成功，待内部确认

    // --- 退款相关 ---
    REFUND_REQUESTED(80, "退款申请中"),      // 用户发起退款申请
    AWAITING_REFUND_CONFIRMATION(90, "待退款确认"), // 退款申请待平台或商家确认
    REFUND_PROCESSING(100, "退款处理中"),    // 退款申请已批准，正在进行退款操作
    REFUND_FAILED(110, "退款失败"),        // 退款操作未能成功
    PARTIALLY_REFUNDED(120, "部分退款"),     // 部分金额已退回给用户
    REFUNDED(130, "已退款");              // 全额金额已退回给用户

    private final int code;
    private final String description;

    OrderAmountStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据 code 获取对应的 OrderAmountStatus 枚举值
     * @param code 状态码
     * @return 对应的枚举值，如果找不到则返回 null
     */
    public static OrderAmountStatus fromCode(int code) {
        for (OrderAmountStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null; // 或者抛出 IllegalArgumentException，取决于你的错误处理策略
    }

    /**
     * 判断是否是已支付状态
     */
    public boolean isPaid() {
        return this == PAID || this == PARTIALLY_PAID || this == PAYMENT_CONFIRMED;
    }

    /**
     * 判断是否是已退款状态
     */
    public boolean isRefunded() {
        return this == REFUNDED || this == PARTIALLY_REFUNDED;
    }

    /**
     * 判断是否是待处理状态
     */
    public boolean isPending() {
        return this == PENDING_PAYMENT || this == AWAITING_REFUND_CONFIRMATION || this == PAYMENT_PROCESSING || this == REFUND_PROCESSING;
    }

    // 可以添加更多辅助方法，例如用于状态流转的判断
    /**
     * 判断当前状态是否可以流转到目标状态 (示例，需要根据实际业务逻辑实现)
     * @param targetStatus 目标状态
     * @return 是否可以流转
     */
    public boolean canTransitionTo(OrderAmountStatus targetStatus) {
        // 这里需要根据你的业务规则定义合法的状态流转
        // 示例：待支付可以流转到支付成功或支付失败
        if (this == PENDING_PAYMENT) {
            return targetStatus == PAID || targetStatus == PAYMENT_FAILED || targetStatus == PAYMENT_INITIATED;
        }
        // 示例：已支付可以流转到退款申请中
        if (this == PAID) {
            return targetStatus == REFUND_REQUESTED;
        }
        // ... 添加其他状态的流转规则
        return false; // 默认不允许流转
    }
}
