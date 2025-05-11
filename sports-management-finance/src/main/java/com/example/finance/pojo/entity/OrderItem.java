package com.example.finance.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.enums.OrderAmountStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单项实体类
 * 记录订单中的每个商品信息，支持单个商品退款
 */
@Data
@TableName("t_order_item")
public class OrderItem {
    
    /**
     * 订单项ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 订单ID，关联订单主表
     */
    private Long orderId;
    
    /**
     * 订单编号
     */
    private String orderNo;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 商品类型：1-场地，2-器材，3-赛事，4-课程
     */
    private Integer productType;
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 商品单价
     */
    private BigDecimal unitPrice;
    
    /**
     * 商品数量
     */
    private Integer quantity;
    
    /**
     * 商品总价
     */
    private BigDecimal totalPrice;
    
    /**
     * 实付金额
     */
    private BigDecimal paidAmount;
    
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
    
    /**
     * 商品状态：由其他模块负责，这里只存储状态码
     */
    private Integer productStatus;
    
    /**
     * 支付状态
     */
    private Integer paymentStatus;
    
    /**
     * 退款状态
     */
    private Integer refundStatus;
    
    /**
     * 退款时间
     */
    private LocalDateTime refundTime;
    
    /**
     * 退款交易号
     */
    private String refundTransactionId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 获取支付状态枚举
     */

    public OrderAmountStatus getPaymentStatusEnum() {
        return paymentStatus != null ? OrderAmountStatus.fromCode(paymentStatus) : null;
    }
    
    /**
     * 获取退款状态枚举
     */

    public OrderAmountStatus getRefundStatusEnum() {
        return refundStatus != null ? OrderAmountStatus.fromCode(refundStatus) : null;
    }
    
    /**
     * 判断订单项是否已支付
     */

    public boolean isPaid() {
        OrderAmountStatus status = getPaymentStatusEnum();
        return status != null && status.isPaid();
    }
    
    /**
     * 判断订单项是否已退款
     */
    public boolean isRefunded() {
        OrderAmountStatus status = getRefundStatusEnum();
        return status != null && status.isRefunded();
    }
    
    /**
     * 判断订单项是否可以退款
     */

    public boolean canRefund() {
        return isPaid() && !isRefunded() && refundAmount.compareTo(totalPrice) < 0;
    }
    
    /**
     * 计算剩余可退款金额
     */

    public BigDecimal getRefundableAmount() {
        return totalPrice.subtract(refundAmount);
    }
}