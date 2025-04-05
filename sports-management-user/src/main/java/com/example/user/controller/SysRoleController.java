package com.example.user.controller;

import com.example.common.response.Result;
import com.example.user.pojo.SysRole;
import com.example.user.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
@Tag(name = "SysRoleController", description = "角色管理接口")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @PostMapping
    @Operation(summary = "创建角色")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysRole> createRole(@RequestBody SysRole sysRole) {
        return sysRoleService.createRole(sysRole);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    public Result<Void> deleteRole(@PathVariable @Parameter(description = "角色ID") Long id) {
        return sysRoleService.deleteRole(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色")
    public Result<SysRole> updateRole(@PathVariable @Parameter(description = "角色ID") Long id, @RequestBody SysRole sysRole) {
        sysRole.setId(id);
        return sysRoleService.updateRole(sysRole);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取角色")
    public Result<SysRole> getRoleById(@PathVariable @Parameter(description = "角色ID") Long id) {
        return sysRoleService.getRoleById(id);
    }

    @GetMapping("/code/{roleCode}")
    @Operation(summary = "根据角色编码获取角色")
    public Result<SysRole> getRoleByCode(@PathVariable @Parameter(description = "角色编码") String roleCode) {
        return sysRoleService.getRoleByCode(roleCode);
    }

    @GetMapping("/list")
    @Operation(summary = "获取所有角色列表")
    public Result<List<SysRole>> getAllRoles() {
        return sysRoleService.getAllRoles();
    }
}
