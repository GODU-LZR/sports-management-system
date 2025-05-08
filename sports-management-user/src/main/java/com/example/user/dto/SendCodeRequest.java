package com.example.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;


@Data
@Schema(description = "发送验证码请求")
public class SendCodeRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "目标邮箱地址", required = true, example = "test@example.com")
    private String email;
}