package com.example.equipment.dto;

import lombok.Data;

/**
 * 对应AI模型返回的JSON结构
 * {
 *   "relative": int, // 1: 相关, 0: 不相关
 *   "conditionScore": int, // 0-100, 100为完好
 *   "description": "string" // 损毁描述
 * }
 */
@Data
public class AiDamageResponse {
    private Integer relative; // 1: 相关, 0: 不相关
    private Integer conditionScore; // 0-100, 100为完好
    private String description; // 损毁描述
}
