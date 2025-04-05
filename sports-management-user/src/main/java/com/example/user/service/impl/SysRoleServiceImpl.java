package com.example.user.service.impl;

import com.example.common.response.Result;
import com.example.common.utils.SnowflakeIdGenerator;
import com.example.user.mapper.SysRoleMapper;
import com.example.user.pojo.SysRole;
import com.example.user.service.SysRoleService;
import com.example.common.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysRoleServiceImpl implements SysRoleService {

    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired(required = false)
    private SnowflakeIdGenerator snowflakeIdGenerator;

    private static final String ROLE_CACHE_KEY_PREFIX = "role:";
    private static final String ROLE_LOCK_KEY_PREFIX = "lock:role:";

    @Override
    public Result<SysRole> createRole(SysRole sysRole) {
        sysRole.setId(snowflakeIdGenerator.nextId());
         // 可以考虑更友好的RoleCode生成策略
        sysRole.setCreateTime(LocalDateTime.now());
        sysRole.setUpdateTime(LocalDateTime.now());
        sysRole.setIsDeleted(0);
        sysRoleMapper.insert(sysRole);

        return Result.success(sysRole);
    }

    @Override
    public Result<Void> deleteRole(Long id) {
        sysRoleMapper.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<SysRole> updateRole(SysRole sysRole) {
        sysRole.setUpdateTime(LocalDateTime.now());
        sysRoleMapper.updateById(sysRole);
        String lockKey = ROLE_LOCK_KEY_PREFIX + sysRole.getId();
        return Result.success(sysRole);
    }

    @Override
    public Result<SysRole> getRoleById(Long id) {
        SysRole sysRole = sysRoleMapper.selectById(id);
        return Result.success(sysRole);
    }

    @Override
    public Result<SysRole> getRoleByCode(String roleCode) {
        return Result.success(sysRoleMapper.selectByRoleCode(roleCode));
    }

    @Override
    public Result<List<SysRole>> getAllRoles() {
        List<SysRole> sysRoles = sysRoleMapper.selectList();
        return Result.success(sysRoles);
    }
}
