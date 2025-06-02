package com.example.venue.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

    // 订单编号
    private String orderId;



    // 用户编号
    private String userId;
    // 用户名称
    private String username;
    // 联系电话
    private String phone;



    // 场地编号
    private String venueId;
    // 场地名称
    private String name;
    // 场地地点描述
    private String position;
    // 租借时的场地价格
    private Double value;
    // 租借开始时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;

    // 租借结束时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
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
