package com.example.equipment.dto;


import com.example.equipment.pojo.EquipmentId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorrowRequestDTO {


    private List<EquipmentId> equipmentIds;  //一次请求 所包含的器材Id

    private Integer quantity;    //申请器材 的数量

    private LocalDateTime startTime;   //借用开始时间

    private LocalDateTime endTime;   //借用结束时间

}
