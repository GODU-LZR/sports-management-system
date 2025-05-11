package com.example.equipment.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserOperateEquipmentDTO {


    private LocalDateTime now;

    private String operation;  //领取或是归还

    private Long equipmentId;

}
