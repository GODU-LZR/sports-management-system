package com.example.equipment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "扫码借取/归还请求体")
public class ScanRequest {

    @Schema(description = "器材图片Base64编码字符串 (包含二维码)")
    private String base64Image;
}
