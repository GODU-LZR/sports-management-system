package com.example.equipment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "生成器材二维码请求DTO")
public class GenerateQrCodeRequestDTO {

    @Schema(description = "器材ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private Long equipmentId; // 对应器材的唯一ID

    // 如果将来需要支持动态跳转URL，可以在这里添加一个baseUrl字段
    // private String baseUrl;
}
