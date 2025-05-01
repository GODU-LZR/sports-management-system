package com.example.equipment.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestVO {

    private Long requestId;  //请求Id

    private Integer quantity;  //审核的数量

    private Long equipmentId;

    private Integer status;   //前端发送过来的 状态

    private Long userId;    //Service层可以填入当前用户Id

    private LocalDateTime startTime;   //借用开始时间

    private LocalDateTime endTime;   //借用结束时间
}
