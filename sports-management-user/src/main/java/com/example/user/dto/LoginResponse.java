package com.example.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginResponse {
    @Schema(description = "JWT令牌", required = true)
    private String token;
    
    // 可选：返回其他用户信息，如用户ID、用户名等
    @Schema(description = "用户ID", example = "1001")
    private Long userId;
}
