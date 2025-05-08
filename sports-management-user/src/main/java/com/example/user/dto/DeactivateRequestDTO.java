package com.example.user.dto; // 确认或修改为你的 DTO 包路径

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "用户注销请求体")
public class DeactivateRequestDTO {

    @NotBlank(message = "邮箱验证码不能为空")
    @Schema(description = "邮箱验证码", required = true, example = "123456")
    private String emailCode;

    @NotBlank(message = "用户密码不能为空")
    @Schema(description = "用户当前密码", required = true, example = "yourPassword123")
    private String password; // 新增密码字段
}