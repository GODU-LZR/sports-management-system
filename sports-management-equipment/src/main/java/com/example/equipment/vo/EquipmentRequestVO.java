package com.example.equipment.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentRequestVO {


    private Long equipmentId;  //申请器材的Id

    private Integer quantity;    //申请器材 的数量

    private LocalDateTime startTime;   //借用开始时间

    private LocalDateTime endTime;   //借用结束时间

}
