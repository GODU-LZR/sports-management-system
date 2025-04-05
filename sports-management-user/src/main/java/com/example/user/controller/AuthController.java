package com.example.user.controller;

import com.example.common.response.Result;
import com.example.user.dto.LoginRequest;
import com.example.user.dto.LoginResponse;
import com.example.user.dto.RegistrationRequest;
import com.example.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "用户登出", description = "登出后使JWT失效")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        return authService.logout(token);
    }
}
