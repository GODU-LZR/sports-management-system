package com.example.user.controller;

import com.example.common.constant.UserConstant;
import com.example.common.response.Result;
import com.example.common.response.ResultCode;
import com.example.user.dto.DeactivateRequestDTO;
import com.example.user.dto.UserProfileDTO;
import com.example.user.pojo.User;
import com.example.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/me")
@Slf4j
@Tag(name = "UserController", description = "用户管理接口")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/avatar") // 使用 /me 表示当前登录用户相关操作
    @Operation(summary = "获取当前用户头像URL", description = "从请求上下文中获取用户ID,查询并返回用户头像URL")
    public Result<String> getCurrentUserAvatar(
            // 通过 UserConstantArgumentResolver 注入当前用户信息
            @Parameter(hidden = true) UserConstant currentUser
    ) {
        log.info("收到获取当前用户头像请求");

        // 1. 检查是否成功获取到用户信息
        if (currentUser == null || currentUser.getUserId() == null) {
            log.warn("无法获取当前用户信息，请检查网关过滤器和 ArgumentResolver 配置");
            // 根据需要决定是返回错误还是 null。返回错误更明确。
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        Long userId = currentUser.getUserId();
        log.info("尝试获取用户 ID: {} 的头像", userId);

        // 2. 调用 Service 层根据 ID 获取用户信息
        Result<User> userResult = userService.getUserById(userId);

        // 3. 处理 Service 返回结果
        if (userResult.getCode() == ResultCode.SUCCESS.getCode() && userResult.getData() != null) {
            User user = userResult.getData();
            String avatarUrl = user.getAvatar();
            if (StringUtils.hasText(avatarUrl)) {
                log.info("成功获取用户 {} 的头像 URL: {}", userId, avatarUrl);
                return Result.success(avatarUrl); // 返回头像 URL 字符串
            } else {
                log.info("用户 {} 存在但未设置头像 URL", userId);
                // 用户存在但没有头像，也算成功，返回 null 或空字符串
                return Result.success(null);
            }
        } else {
            // Service 层返回错误或未找到用户
            log.warn("获取用户 {} 信息失败或用户不存在，Service 返回: code={}, message={}",
                    userId, userResult.getCode(), userResult.getMessage());
            // 可以根据 userResult 的 code 和 message 返回更具体的错误
            return Result.error(userResult.getCode(), userResult.getMessage()); // 将 Service 的错误信息透传回去
            // 或者返回统一错误： return Result.error(ResultCode.ERROR, "获取用户信息失败");
        }
    }

    // --- 新增 /me 接口 ---
    @GetMapping
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细个人信息及其角色代码")
    public Result<UserProfileDTO> getCurrentUserInfo(
            @Parameter(hidden = true) UserConstant currentUser) {
        log.info("收到获取当前用户信息的请求 (DTO + Mapper 版本)");

        if (currentUser == null || currentUser.getUserId() == null) {
            log.warn("无法获取当前用户信息 (未登录或 Token 无效)");
            return Result.error(ResultCode.UNAUTHORIZED); // 使用您定义的 UNAUTHORIZED
        }

        Long currentUserId = currentUser.getUserId();
        log.info("尝试获取用户 ID: {} 的 Profile DTO", currentUserId);

        // 调用返回 DTO 的 Service 方法
        return userService.getUserProfileById(currentUserId);
    }

    // --- 新增/修改：用于当前用户更新自己的 Profile ---
    @PatchMapping
    @Operation(summary = "更新当前用户信息", description = "部分更新当前登录用户的个人信息，可包含新头像 URL")
    public Result<UserProfileDTO> updateCurrentUserProfile(
            @Parameter(hidden = true) UserConstant currentUser,
            @RequestBody Map<String, Object> updateData // 接收 Map
    ) {
        if (currentUser == null || currentUser.getUserId() == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        Long currentUserId = currentUser.getUserId();
        log.info("收到更新用户 ID: {} 的 Profile 请求，更新字段: {}", currentUserId, updateData.keySet());

        // 调用 Service 层进行更新
        return userService.updateCurrentUserProfile(currentUserId, updateData);
    }

    /**
     * 注销（软删除）当前登录用户
     * @param currentUser 当前用户信息（由参数解析器注入）
     * @param request 包含邮箱验证码和密码的请求体 DTO
     * @return 操作是否成功
     */
    @PostMapping("/deactivate") // 保持 POST 请求
    @Operation(summary = "注销当前用户账户", description = "软删除当前用户，并移除角色关联，需要在请求体中提供邮箱验证码和当前密码进行验证。")
    public Result<Boolean> deactivateCurrentUser(
            @Parameter(hidden = true) UserConstant currentUser,
            @Valid @RequestBody DeactivateRequestDTO request // 接收包含密码和验证码的 DTO
    ) {
        log.info("收到注销当前用户的请求 (使用请求体传递验证码和密码)");

        // 1. 检查用户是否登录
        if (currentUser == null || currentUser.getUserId() == null) {
            log.warn("无法注销：未获取到当前用户信息 (未登录或 Token 无效)");
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        Long currentUserId = currentUser.getUserId();
        String emailCode = request.getEmailCode();
        String password = request.getPassword(); // 从 DTO 获取密码

        log.info("用户 ID: {} 正在请求注销账户...", currentUserId); // 日志中不打印密码和验证码

        // 2. 调用 Service 层处理注销逻辑，传入 password
        return userService.deactivateCurrentUser(currentUserId, emailCode, password); // 传递 password
    }

}
