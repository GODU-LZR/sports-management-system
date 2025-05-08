package com.example.user.controller;

import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.user.dto.LoginRequest;
import com.example.user.dto.LoginResponse;
import com.example.user.dto.RegistrationRequest;
import com.example.user.dto.SendCodeRequest;
import com.example.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@Tag(name = "AuthController", description = "用户鉴权接口")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册成功后自动登录并返回JWT")
    public Result<LoginResponse> register(@RequestBody RegistrationRequest registrationRequest) {
        return authService.register(registrationRequest);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用邮箱和密码进行登录")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "登出后使用户凭证失效")
    public Result<Void> logout(
            // 使用 @Parameter(hidden = true) 让 Swagger UI 忽略这个参数，因为它是由 ArgumentResolver 自动注入的
            // 这个参数的注入依赖于你的 UserConstantArgumentResolver 能正确工作
            @Parameter(hidden = true) UserConstant currentUser
    ) {
        // 增加健壮性检查，确保 ArgumentResolver 成功注入了用户信息
        if (currentUser == null || currentUser.getUserId() == null) {
            log.warn("登出接口未能获取到用户信息 (UserConstant 为 null 或 userId 为 null)，请检查网关过滤器和 ArgumentResolver 配置。");
            // 即使获取不到用户信息，也让前端认为登出成功，避免前端卡住
            // 但这种情况表示系统配置有问题，需要关注日志
            return Result.success();
            // 或者返回错误： return Result.error(ResultCode.UNAUTHORIZED, "无法识别当前用户");
        }
        log.info("Controller 收到登出请求 for userId: {}", currentUser.getUserId());
        // 调用修改后的 Service 方法，传入 userId
        return authService.logout(currentUser.getUserId());
    }

    @PostMapping("/sendVerificationCode")
    @Operation(summary = "发送邮箱验证码 (通过 Dubbo)", description = "调用中间件服务发送验证码")
    @ApiResponse(responseCode = "200", description = "返回是否成功调用服务以及相关信息")
    public Result<Boolean> triggerSendCode(
            @Parameter(description = "目标邮箱地址", required = true, example = "test@example.com")
            @RequestParam String email) {
        return authService.sendVerificationEmail(email);
    }
}
