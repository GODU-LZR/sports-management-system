package com.example.equipment.dto.utilDTO;

import lombok.Data;

@Data
public class EquipmentPageQuery {
    private Integer pageNum = 1; // 当前页码，默认第1页
    private Integer pageSize = 10; // 每页记录数，默认10条
    private String name;   //根据器材分裂的名称查询

    private Integer status;   //器材状态
    // 你可以根据需要添加其他查询字段，例如：

}
