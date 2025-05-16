package com.example.equipment.dto;

import lombok.Data;

/**
 * Service层返回给Controller的损毁评估结果
 */
@Data
public class AssessDamageResult {
    private Long equipmentId;
    private Integer conditionScore; // 0-100
    private String damageDescription; // AI返回的损毁描述
    private String message; // 结果消息
    private boolean success; // 是否成功
    private Integer relative; // AI判断的相关性 (1:相关, 0:不相关)
}
