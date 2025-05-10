package com.example.equipment.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectUserRequestDTO {

    private String equipmentName;  //根据器材分类名称筛选请求

    private Integer status;  //根据请求的状态筛选   用01234分别表示‘审核中’、‘已通过’、‘已拒绝’、‘已归还’、‘已撤销’

}
