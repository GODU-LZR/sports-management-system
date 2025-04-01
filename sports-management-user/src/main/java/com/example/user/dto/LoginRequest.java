package com.example.user.dto;

import io.swagger.v3.oas.annotations.media.Schema; // 引入 OpenAPI 3 的 Schema
import lombok.Data;

@Data
@Schema(description = "登录请求参数") // 使用 Schema 描述模型
public class LoginRequest {

    @Schema(description = "用户邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "test@example.com") // 使用 Schema 描述字段
    private String email;

    @Schema(description = "用户密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "yourpassword") // 使用 Schema 描述字段
    private String password;

    /**
     * 客户端指纹 (由前端计算并传入)
     * 新增字段
     */
    @Schema(
            description = "客户端指纹",
            requiredMode = Schema.RequiredMode.REQUIRED, // 标记为必需
            example = "ad4ca8ea7bc1709e8f7bb1ddff78c1900ca81fb4063aebd0a5f44df7e1944c22", // 示例
            externalDocs = @io.swagger.v3.oas.annotations.ExternalDocumentation(description = "前端通过 SHA-256(User-Agent) 计算") // 可以用 externalDocs 或 description 补充说明
    )
    private String clientFingerprint;
}
