package com.example.middleware.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Schema(description = "校验验证码请求")
public class VerifyCodeRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "目标邮箱地址", required = true, example = "test@example.com")
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Size(min = 6, max = 6, message = "验证码必须是6位") // 根据你的验证码长度调整
    @Schema(description = "用户提交的6位验证码", required = true, example = "A1B2C3")
    private String code;
}