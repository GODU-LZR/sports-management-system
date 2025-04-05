package com.example.user.service.impl;

import com.example.common.response.Result;
import com.example.common.utils.SnowflakeIdGenerator;
import com.example.user.mapper.SysRoleMapper;
import com.example.user.mapper.SysUserRoleMapper;
import com.example.user.mapper.UserMapper;
import com.example.user.pojo.SysRole;
import com.example.user.pojo.SysUserRole;
import com.example.user.pojo.User;
import com.example.user.service.UserService;
import com.example.common.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired(required = false)
    private SnowflakeIdGenerator snowflakeIdGenerator;

    private static final String USER_CACHE_KEY_PREFIX = "user:";
    private static final String USER_LOCK_KEY_PREFIX = "lock:user:";

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> createUser(User user) {
        // 生成ID和UserCode
        long userId = snowflakeIdGenerator.nextId();
        user.setId(userId);
        user.setUserCode(UUID.randomUUID().toString());
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(user.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setIsDeleted(0);
        user.setStatus(0); // 默认正常状态

        // 插入用户
        userMapper.insert(user);

        // 默认赋予USER角色
        SysRole defaultRole = sysRoleMapper.selectByRoleCode("USER");
        if (defaultRole != null) {
            SysUserRole userRole = new SysUserRole();
            userRole.setId(snowflakeIdGenerator.nextId());
            userRole.setUserId(userId);
            userRole.setRoleId(defaultRole.getId());
            userRole.setCreateTime(LocalDateTime.now());
            userRole.setCreatorId(1L); // 默认创建人ID
            sysUserRoleMapper.insert(userRole);
        }


        return Result.success(user);
    }

    @Override
    public Result<Void> deleteUser(Long id) {
        // 删除数据库记录
        userMapper.deleteById(id);
        sysUserRoleMapper.deleteByUserId(id); // 删除用户角色关联关系
        return Result.success();
    }

    @Override
    public Result<User> updateUser(User user) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(user.getPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success(user);
    }

    @Override
    public Result<User> getUserById(Long id) {

        User user = userMapper.selectById(id);
        return Result.success(user);
    }

    @Override
    public Result<User> getUserByUsername(String username) {
        return Result.success(userMapper.selectByUsername(username)); // 不缓存用户名查询，因为用户名可能更改
    }

    @Override
    public Result<User> getUserByEmail(String email) {
        return Result.success(userMapper.selectByEmail(email));
    }

    @Override
    public Result<List<User>> getAllUsers() {
        List<User> users = userMapper.selectList();
        return Result.success(users);
    }
}
