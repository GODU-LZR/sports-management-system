package com.example.equipment.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentVO {

    private Long createId;  //创建者Id

    private Long modifiedId;  //修改者id

    private Long equipmentId; //器材Id

    private String equipmentName;  //器材名称

    private String pictureUrl;   // 器材图片

    private Integer value;       //器材价格  小时/元

    private Integer total;       //器材总量

    private Integer stock;       // 器材库存

    private Integer isSale;      //是否 已出售

    private Integer isDeleted;   //是否已删除

    private LocalDateTime createTime;   //创建时间

    private LocalDateTime modifiedTime;   //修改时间
}
