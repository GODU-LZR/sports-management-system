package com.example.equipment.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("equipment_request")
public class RequestVO {

    private Long requestId;  //请求Id

    private Long userId;

    private Long equipmentId;

    private Integer quantity;  //审核的数量

    private LocalDateTime startTime;   //借用开始时间

    private LocalDateTime endTime;   //借用结束时间

    private Integer status;   // 状态

    private LocalDateTime createTime;
    private LocalDateTime modifiedTime;
    private Long cancellerId;
    private Long reviewId;
    private Integer isRevoked;
}
