package com.example.venue.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderAddDto {

    // 雪花生成数得到的订单编号
    private String orderId;

    // 场地编号
    private String venueId;

    // 下单人的用户编号
    private String userId;

    // 联系电话
    private String phone;

    // 租借开始时间
    private LocalDateTime startTime;

    // 租借结束时间
    private LocalDateTime endTime;


    // 将OrderAddRequest转为OrderAddDto格式
    public static OrderAddDto fromRequest(OrderAddRequest request) {
        OrderAddDto dto = new OrderAddDto();
        // 直接拷贝相同属性
        dto.setVenueId(request.getVenueId());
        dto.setPhone(request.getPhone());
        dto.setStartTime(request.getStartTime());
        dto.setEndTime(request.getEndTime());
        return dto;
    }

    public static OrderAddDto fromRequest(OrderReplaceRequest request) {
        OrderAddDto dto = new OrderAddDto();
        // 直接拷贝同名属性
        dto.setOrderId(request.getOrderId());  // 订单编号
        dto.setVenueId(request.getVenueId());  // 场地编号
        dto.setStartTime(request.getStartTime());
        dto.setEndTime(request.getEndTime());
        return dto;
    }
}
