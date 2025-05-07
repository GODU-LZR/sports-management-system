package com.example.equipment.vo;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
//@TableName("equipment")
public class EquipmentVO {

//    private Long createId;  //创建者Id

//    private Long modifiedId;  //修改者id

    private Long equipmentId; //器材Id

    private String categoryId;  //器材分类

    private String name;

    private String pictureUrl;   // 器材图片

    private float value;   //该器材的借用 价格  1小时

    private  Integer total;    //总数

    private  Integer stock;    //库存

  private String specification;    //器材的描述

  private Integer status;   //器材状态

    private Integer isDeleted;   //是否已删除

    private LocalDateTime createTime;   //创建时间

    private LocalDateTime modifiedTime;   //修改时间

}
