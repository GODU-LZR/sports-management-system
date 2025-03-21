package com.example.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Value("${spring.application.name:}")
    private String applicationName;

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
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
    }

//    @Bean
//    public OperationCustomizer addCustomHeaders() {
//        return (operation, handlerMethod) -> {
//            // 添加 X-User-Roles 请求头，用于下游服务获取用户角色信息
//            operation.addParametersItem(new Parameter()
//                    .in("header")
//                    .name("X-User-Roles")
//                    .description("User roles for authorization (e.g., ADMIN,USER)")
//                    .required(false)
//                    .schema(new io.swagger.v3.oas.models.media.StringSchema()));
//            return operation;
//        };
//    }
}
