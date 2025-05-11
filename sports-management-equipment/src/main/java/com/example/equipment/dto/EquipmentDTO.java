package com.example.equipment.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentDTO {

    private Long equipmentId;  //器材ID

//    private String name;

    private String pictureUrl;   // 器材图片

    private String specification;   //规格描述

//    private Integer isSale;      //是否 已出售
//
//    private Integer isDeleted;   //是否已删除
//
//    private LocalDateTime createTime;   //创建时间
//
//    private LocalDateTime modifiedTime;   //修改时间
}
