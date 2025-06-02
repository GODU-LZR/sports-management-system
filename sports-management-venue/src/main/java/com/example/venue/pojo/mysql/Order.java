package com.example.venue.pojo.mysql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data // 自动生成 getter, setter, equals, hashCode, toString 方法
@NoArgsConstructor // 自动生成无参构造方法
@AllArgsConstructor // 自动生成包含所有参数的构造方法
public class Order {

    // 订单编号
    private String orderId;

    // 用户编号
    private String userId;

    // 场地编号
    private String venueId;

    // 联系电话
    private String phone;

    // 租借时的场地单价
    private Double value;

    // 租借开始时间
    private LocalDateTime startTime;

    // 租借结束时间
    private LocalDateTime endTime;

    // 下单时间
    private LocalDateTime orderTime;

    // 付款时间
    private LocalDateTime payTime;

    // 审核人的用户编号
    private String auditId;

    // 审核人名称(适当冗余,减少访问其他模块的次数)
    private String auditName;

    /**
     * 审核状态
     * 0: 待审核, 1: 已通过, 2: 已否决, 3: 已撤销, 4: 更换场地
     */
    private Integer state;

    /**
     * 付款状态
     * 0: 待支付, 1: 已支付
     */
    private Integer payState;

    // 付款金额
    private Double payment;
}
