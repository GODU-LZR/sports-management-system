package com.example.equipment.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Equipment {

    private Long equipmentId;

    private String equipmentName;

    private String pictureUrl;

    private Integer value;

    private Integer total;

    private Integer stock;

    private Integer isSale;

    private Integer isDeleted;
}
