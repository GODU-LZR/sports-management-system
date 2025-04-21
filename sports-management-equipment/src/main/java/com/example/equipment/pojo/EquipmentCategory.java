package com.example.equipment.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentCategory {

    private Long categoryId;

    private  String name;

    private String description;

    private float value;

    private  Integer total;

    private  Integer stock;

    private LocalDateTime createTime;

    private LocalDateTime modifiedTime;

    private Long createId;

    private Long modifiedId;

}
