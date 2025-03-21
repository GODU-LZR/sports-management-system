package com.example.user.controller;

import com.example.common.model.Result;
import com.example.user.pojo.SysUserRole;
import com.example.user.service.SysUserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-role")
@Tag(name = "SysUserRoleController", description = "用户角色关联管理接口")
public class SysUserRoleController {

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @PostMapping
    @Operation(summary = "创建用户角色关联")
    public Result<SysUserRole> createUserRole(@RequestBody SysUserRole sysUserRole) {
        return sysUserRoleService.createUserRole(sysUserRole);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户角色关联")
    public Result<Void> deleteUserRole(@PathVariable @Parameter(description = "用户角色关联ID") Long id) {
        return sysUserRoleService.deleteUserRole(id);
    }

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "根据用户ID删除用户角色关联")
    public Result<Void> deleteUserRolesByUserId(@PathVariable @Parameter(description = "用户ID") Long userId) {
        return sysUserRoleService.deleteUserRolesByUserId(userId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取用户角色关联")
    public Result<SysUserRole> getUserRoleById(@PathVariable @Parameter(description = "用户角色关联ID") Long id) {
        return sysUserRoleService.getUserRoleById(id);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID获取用户角色列表")
    public Result<List<SysUserRole>> getUserRolesByUserId(@PathVariable @Parameter(description = "用户ID") Long userId) {
        return sysUserRoleService.getUserRolesByUserId(userId);
    }

    @GetMapping("/role/{roleId}")
    @Operation(summary = "根据角色ID获取用户角色列表")
    public Result<List<SysUserRole>> getUserRolesByRoleId(@PathVariable @Parameter(description = "角色ID") Long roleId) {
        return sysUserRoleService.getUserRolesByRoleId(roleId);
    }

    @GetMapping("/list")
    @Operation(summary = "获取所有用户角色关联列表")
    public Result<List<SysUserRole>> getAllUserRoles() {
        return sysUserRoleService.getAllUserRoles();
    }
}
