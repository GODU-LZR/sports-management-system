package com.example.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RegistrationRequest {
    @Schema(description = "用户邮箱", example = "user@example.com", required = true)
    private String email;

    @Schema(description = "用户密码", required = true)
    private String password;

    @Schema(description = "用户名", example = "张三", required = true)
    private String username;

    @Schema(description = "真实姓名", example = "张三", required = true)
    private String realName;

    @Schema(description = "头像URL", required = true)
    private String avatar;

    @Schema(description = "验证码", required = true)
    private String verifyCode;
}
