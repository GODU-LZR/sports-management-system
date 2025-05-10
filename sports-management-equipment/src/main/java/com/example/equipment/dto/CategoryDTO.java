package com.example.equipment.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {

    private Long categoryId;

    private  String name;  //类别名称

    private String description;  //分类描述

    private float value;   //该类器材的借用 价格  半个小时

//    private  Integer total;    //总数

//    private  Integer stock;    //库存

}
