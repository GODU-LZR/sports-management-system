package com.example.user.controller;

import com.example.common.response.Result;
import com.example.user.pojo.User;
import com.example.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "UserController", description = "用户管理接口")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    @Operation(summary = "创建用户")
    public Result<User> createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> deleteUser(@PathVariable @Parameter(description = "用户ID") Long id) {
        return userService.deleteUser(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public Result<User> updateUser(@PathVariable @Parameter(description = "用户ID") Long id, @RequestBody User user) {
        user.setId(id); // 确保ID一致
        return userService.updateUser(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取用户")
    public Result<User> getUserById(@PathVariable @Parameter(description = "用户ID") Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名获取用户")
    public Result<User> getUserByUsername(@PathVariable @Parameter(description = "用户名") String username) {
        return userService.getUserByUsername(username);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "根据邮箱获取用户")
    public Result<User> getUserByEmail(@PathVariable @Parameter(description = "邮箱") String email) {
        return userService.getUserByEmail(email);
    }

    @GetMapping("/list")
    @Operation(summary = "获取所有用户列表")
    public Result<List<User>> getAllUsers() {
        return userService.getAllUsers();
    }
}
