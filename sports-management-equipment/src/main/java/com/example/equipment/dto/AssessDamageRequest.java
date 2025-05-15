package com.example.equipment.dto;

import lombok.Data;

@Data
public class AssessDamageRequest {
    private String base64Image; // 前端上传的图片 Base64 字符串
}
