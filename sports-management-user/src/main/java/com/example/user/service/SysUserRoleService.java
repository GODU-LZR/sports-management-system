package com.example.user.service;

import com.example.common.model.Result;
import com.example.user.pojo.SysUserRole;

import java.util.List;

public interface SysUserRoleService {
    Result<SysUserRole> createUserRole(SysUserRole sysUserRole);
    Result<Void> deleteUserRole(Long id);
    Result<Void> deleteUserRolesByUserId(Long userId);
    Result<SysUserRole> getUserRoleById(Long id);
    Result<List<SysUserRole>> getUserRolesByUserId(Long userId);
    Result<List<SysUserRole>> getUserRolesByRoleId(Long roleId);
    Result<List<SysUserRole>> getAllUserRoles();
}
