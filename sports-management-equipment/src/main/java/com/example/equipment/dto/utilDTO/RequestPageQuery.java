package com.example.equipment.dto.utilDTO;

import lombok.Data;

@Data
public class RequestPageQuery {
    private Integer pageNum = 1; // 当前页码，默认第1页
    private Integer pageSize = 10; // 每页记录数，默认10条
    // 如果将来需要按申请状态、器材类型等过滤，可以在这里添加字段
     private Integer status; // 申请状态
    // private String equipmentName; // 器材名称（如果需要在申请表中关联或存储）
}
