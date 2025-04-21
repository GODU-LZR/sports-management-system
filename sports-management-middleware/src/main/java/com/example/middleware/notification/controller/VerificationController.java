package com.example.middleware.notification.controller;


import com.example.common.services.VerificationCodeService;
import com.example.middleware.notification.dto.SendCodeRequest;
import com.example.middleware.notification.dto.VerifyCodeRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid; // 注意是 javax.validation 或 jakarta.validation

@RestController
@RequestMapping("/verification") // API 基础路径
@Tag(name = "验证码服务", description = "提供邮箱验证码的发送和校验功能") // OpenAPI Tag
@Validated // 开启方法级别参数校验（如果需要对非 @RequestBody 参数校验）
@Slf4j
public class VerificationController {

    @Autowired
    private VerificationCodeService verificationCodeService;

    @PostMapping("/sendCode")
    @Operation(summary = "发送邮箱验证码", description = "向指定邮箱发送一个6位数的验证码，验证码120秒内有效。") // OpenAPI 操作描述
    @ApiResponse(responseCode = "200", description = "验证码发送任务已成功触发", content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "400", description = "请求参数错误 (如邮箱格式无效)")
    @ApiResponse(responseCode = "500", description = "服务器内部错误 (如连接 Redis 或 RabbitMQ 失败)")
    public ResponseEntity<String> sendVerificationCode(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "包含目标邮箱地址的请求体", required = true, content = @Content(schema = @Schema(implementation = SendCodeRequest.class)))
            @Valid @RequestBody SendCodeRequest request // @Valid 开启对 DTO 内注解的校验
    ) {
        log.info("收到发送验证码请求，邮箱: {}", request.getEmail());
        boolean success = verificationCodeService.sendCode(request.getEmail());
        if (success) {
            return ResponseEntity.ok("验证码发送任务已触发，请检查邮箱。");
        } else {
            // 可以根据 Service 层返回的更具体错误信息来返回不同的状态码
            return ResponseEntity.internalServerError().body("发送验证码失败，请稍后重试。");
        }
    }

    @PostMapping("/verifyCode")
    @Operation(summary = "校验邮箱验证码", description = "校验用户提交的邮箱验证码是否正确且在有效期内。")
    @ApiResponse(responseCode = "200", description = "校验结果 (true: 成功, false: 失败或过期)", content = @Content(schema = @Schema(implementation = Boolean.class)))
    @ApiResponse(responseCode = "400", description = "请求参数错误 (如邮箱格式无效、验证码格式错误)")
    public ResponseEntity<Boolean> verifyVerificationCode(
             @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "包含邮箱和验证码的请求体", required = true, content = @Content(schema = @Schema(implementation = VerifyCodeRequest.class)))
            @Valid @RequestBody VerifyCodeRequest request
    ) {
        log.info("收到校验验证码请求，邮箱: {}, 验证码: {}", request.getEmail(), request.getCode());
        boolean isValid = verificationCodeService.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(isValid);
    }
}