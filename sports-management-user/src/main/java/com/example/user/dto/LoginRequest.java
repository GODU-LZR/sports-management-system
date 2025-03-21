package com.example.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginRequest {
    @Schema(description = "用户邮箱", example = "user@example.com", required = true)
    private String email;

    @Schema(description = "用户密码", required = true)
    private String password;
}
