package com.example.equipment.vo;


import com.example.equipment.pojo.BorrowEquipment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminRequestVO {

    private Long requestId;  //请求Id

    private String userName;   //用户名称

    private Long userId;

    private List<BorrowEquipment> equipmentList;  //用户预约器材的类型名称  篮球  排球

    private LocalDateTime startTime;   //借用开始时间

    private LocalDateTime endTime;   //借用结束时间

    private Integer status;   // 状态

    private LocalDateTime createTime;

    private LocalDateTime modifiedTime;

    private Long cancellerId;

    private Long reviewId;

    private Integer isRevoked;
}
