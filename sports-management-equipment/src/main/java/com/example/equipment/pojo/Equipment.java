package com.example.equipment.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Equipment {

    private Long equipmentId;  //一个器材对应的ID

    private  Long categoryId; //一个器材对应的种类

    private String pictureUrl;  //器材的图片

    private String specification;  //器材的规格描述

    private Integer status;    //器材所属的状态  1: 可用。2: 维修中。3: 报废

    private LocalDateTime createTime;

    private LocalDateTime modifiedTime;

    private Integer isDeleted;  //是否删除

    private Long createId;

    private Long modifiedId;
}
