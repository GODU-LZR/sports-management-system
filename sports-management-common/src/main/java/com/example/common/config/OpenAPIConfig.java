package com.example.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${spring.application.name:}")
    private String applicationName;

    // 定义不需要客户端指纹的操作ID列表(登录、注册等)
    private static final List<String> FINGERPRINT_EXCLUDED_OPERATIONS = Arrays.asList(
            "login", "register", "logout",
            "getApiDocs", "getSwaggerResources"
    );

    @Bean
    public OpenAPI customizeOpenAPI() {
        // 网关统一前缀为 /api/，并且根据应用名称去掉 "-service"
        String gatewayPath = "/api/" + applicationName.replace("-service", "");

        return new OpenAPI()
                .addServersItem(new Server().url(gatewayPath))
                .components(new Components()
                        // 修改为 Bearer Token 认证
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        // 添加客户端指纹作为 API Key 认证方式
                        .addSecuritySchemes("ClientFingerprint", new SecurityScheme()
                                .type(Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Client-Fingerprint")
                                .description("客户端指纹 (User-Agent的SHA-256哈希值)")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("BearerAuth")
                        .addList("ClientFingerprint")); // 将指纹也添加为全局安全要求
    }

    /**
     * 自定义操作配置器，为需要认证的API添加客户端指纹请求头参数
     */
    @Bean
    public OperationCustomizer fingerprintOperationCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            // 检查操作是否需要指纹
            String operationId = operation.getOperationId();
            boolean excluded = operationId != null && FINGERPRINT_EXCLUDED_OPERATIONS.contains(operationId);

            // 如果不在排除列表中且需要认证 (已经有安全要求)
            if (!excluded && operation.getSecurity() != null && !operation.getSecurity().isEmpty()) {
                // 手动添加指纹头参数，使其在Swagger UI中可输入
                HeaderParameter fingerprintHeader = (HeaderParameter) new HeaderParameter()
                        .name("X-Client-Fingerprint")
                        .description("客户端指纹 (User-Agent的SHA-256哈希值)")
                        .required(true);

                // 创建Schema并设置示例值
                Schema<String> schema = new Schema<String>();
                schema.setType("string");
                schema.setExample("ad4ca8ea7bc1709e8f7bb1ddff78c1900ca81fb4063aebd0a5f44df7e1944c22");
                fingerprintHeader.setSchema(schema);

                // 添加到操作的参数列表
                operation.addParametersItem(fingerprintHeader);

                // 添加说明文本到描述中
                String currentDescription = operation.getDescription();
                String fingerprintNote = "**注意**: 此接口需要客户端指纹验证，请在请求头中传入`X-Client-Fingerprint`。";
                if (currentDescription != null && !currentDescription.isEmpty()) {
                    operation.setDescription(currentDescription + "\n\n" + fingerprintNote);
                } else {
                    operation.setDescription(fingerprintNote);
                }
            }

            return operation;
        };
    }
}
