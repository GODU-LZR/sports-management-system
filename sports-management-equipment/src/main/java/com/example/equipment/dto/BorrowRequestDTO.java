package com.example.equipment.dto;


import com.example.equipment.pojo.BorrowEquipment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorrowRequestDTO {


    //请求里面包含的器材名称 对应的数量
    private List<BorrowEquipment> equipmentList;  //用户预约器材的类型名称  篮球  排球

    private LocalDateTime startTime;   //借用开始时间

    private LocalDateTime endTime;   //借用结束时间

}
