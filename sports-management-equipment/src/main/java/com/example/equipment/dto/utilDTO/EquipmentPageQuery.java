package com.example.equipment.dto.utilDTO;

import lombok.Data;

@Data
public class EquipmentPageQuery {
    private Integer pageNum = 1; // 当前页码，默认第1页
    private Integer pageSize = 10; // 每页记录数，默认10条
    private String specification; // 器材名称的模糊查询关键字
    // 你可以根据需要添加其他查询字段，例如：
    // private String type; // 器材类型的模糊查询关键字

}
