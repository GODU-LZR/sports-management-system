package com.example.equipment.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentCategory {

    private Long categoryId;  //器材分类ID

    private  String name;  //类别名称

    private String description;  //分类描述

    private float value;   //该类器材的借用 价格  1小时

    private  Integer total;    //总数

    private  Integer stock;    //库存

    private LocalDateTime createTime;

    private LocalDateTime modifiedTime;

    private Long createId;

    private Long modifiedId;

}
