package com.example.finance.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.enums.OrderAmountStatus;

import com.example.finance.enums.OrderType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单主表实体类
 * 记录订单基本信息，一个订单可以包含多个订单项
 */
@Data
@TableName("t_order")
public class Order {

 /**
  * 订单ID，使用雪花算法生成
  */
 @TableId(value = "id", type = IdType.ASSIGN_ID)
 private Long id;

 /**
  * 订单编号，业务可读编号
  */
 private String orderNo;

 /**
  * 用户ID
  */
 private Long userId;

 /**
  * 用户名称
  */
 private String userName;

 /**
  * 订单类型
  */
 private OrderType orderType;

 /**
  * 订单状态
  */
 private OrderAmountStatus orderStatus;

 /**
  * 支付状态
  */
 private Integer paymentStatus;

 /**
  * 订单总金额
  */
 private BigDecimal totalAmount;

 /**
  * 实付金额
  */
 private BigDecimal paidAmount;

 /**
  * 退款金额
  */
 private BigDecimal refundAmount;

 /**
  * 支付方式：1-支付宝，2-微信，3-银行卡等
  */
 private Integer paymentMethod;

 /**
  * 支付时间
  */
 private LocalDateTime paymentTime;

 /**
  * 支付交易号
  */
 private String transactionId;

 /**
  * 订单备注
  */
 private String remark;

 /**
  * 创建时间
  */
 private LocalDateTime createTime;

 /**
  * 更新时间
  */
 private LocalDateTime updateTime;

 /**
  * 订单项列表（非数据库字段）
  */
 @TableField(exist = false)
 private List<OrderItem> orderItems;


}