package com.example.user.service.impl;

import com.example.common.model.Result;
import com.example.common.utils.SnowflakeIdGenerator;
import com.example.user.mapper.SysUserRoleMapper;
import com.example.user.pojo.SysUserRole;
import com.example.user.service.SysUserRoleService;
import com.example.user.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysUserRoleServiceImpl implements SysUserRoleService {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired(required = false)
    private SnowflakeIdGenerator snowflakeIdGenerator;

    private static final String USER_ROLE_CACHE_KEY_PREFIX = "userRole:";

    @Override
    public Result<SysUserRole> createUserRole(SysUserRole sysUserRole) {
        sysUserRole.setId(snowflakeIdGenerator.nextId());
        sysUserRole.setCreateTime(LocalDateTime.now());
        sysUserRoleMapper.insert(sysUserRole);
        // 关联表通常不缓存，或者根据具体业务场景决定是否缓存
        return Result.success(sysUserRole);
    }

    @Override
    public Result<Void> deleteUserRole(Long id) {
        sysUserRoleMapper.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<Void> deleteUserRolesByUserId(Long userId) {
        sysUserRoleMapper.deleteByUserId(userId);
        return Result.success();
    }

    @Override
    public Result<SysUserRole> getUserRoleById(Long id) {
        // 关联表通常不缓存，或者根据具体业务场景决定是否缓存
        return Result.success(sysUserRoleMapper.selectById(id));
    }

    @Override
    public Result<List<SysUserRole>> getUserRolesByUserId(Long userId) {
        // 用户角色列表可以考虑缓存，如果用户角色变动不频繁
        return Result.success(sysUserRoleMapper.selectByUserId(userId));
    }

    @Override
    public Result<List<SysUserRole>> getUserRolesByRoleId(Long roleId) {
        return Result.success(sysUserRoleMapper.selectByRoleId(roleId));
    }

    @Override
    public Result<List<SysUserRole>> getAllUserRoles() {
        return Result.success(sysUserRoleMapper.selectList());
    }
}
