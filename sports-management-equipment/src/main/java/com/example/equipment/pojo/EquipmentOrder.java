package com.example.equipment.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentOrder {

    private Long orderId;  //订单编号

    private Long userId;  //创建订单的用户Id

    private Integer reviewStatus;  //订单的审核状态

    private String reviewerName;  //审核人的名字

    private Integer payMent;    //付款金额

    private LocalDateTime paymentTime;  //付款时间

    private LocalDateTime returnTime;  //归还时间

    private LocalDateTime createTime;  //创建时间

    private LocalDateTime modifiedTime; //修改时间

    private Integer isDeleted;  // 删除状态

    private Long cancellerId;  // 撤销者Id

}
