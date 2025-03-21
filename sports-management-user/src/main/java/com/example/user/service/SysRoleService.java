package com.example.user.service;

import com.example.common.model.Result;
import com.example.user.pojo.SysRole;

import java.util.List;

public interface SysRoleService {
    Result<SysRole> createRole(SysRole sysRole);
    Result<Void> deleteRole(Long id);
    Result<SysRole> updateRole(SysRole sysRole);
    Result<SysRole> getRoleById(Long id);
    Result<SysRole> getRoleByCode(String roleCode);
    Result<List<SysRole>> getAllRoles();
}
