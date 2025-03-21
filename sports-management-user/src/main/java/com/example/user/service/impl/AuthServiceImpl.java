package com.example.user.service.impl;

import com.example.common.model.Result;
import com.example.common.model.ResultCode;
import com.example.common.model.UserRoleWrapper;
import com.example.common.utils.SnowflakeIdGenerator;
import com.example.user.dto.LoginRequest;
import com.example.user.dto.LoginResponse;
import com.example.user.dto.RegistrationRequest;
import com.example.user.mapper.SysRoleMapper;
import com.example.user.mapper.SysUserRoleMapper;
import com.example.user.mapper.UserMapper;
import com.example.user.pojo.SysRole;
import com.example.user.pojo.SysUserRole;
import com.example.user.pojo.User;
import com.example.user.service.AuthService;
import com.example.user.service.UserService;
import com.example.user.utils.JwtUtil;
import com.example.user.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserService userService; // 提供根据邮箱查询、创建用户的业务

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired(required = false)
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Autowired
    private RedisUtil redisUtil;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    @Transactional
    public Result<LoginResponse> register(RegistrationRequest registrationRequest) {
        // 1. 检查邮箱是否已被注册
        Result<User> existingUserResult = userService.getUserByEmail(registrationRequest.getEmail());
        if (existingUserResult.getData() != null) {
            return Result.error("邮箱已被注册");
        }

        // 2. 构造 User 对象
        User user = new User();
        // 生成雪花算法ID（可调用 userService 或自己生成，此处简化处理）
        long userId = snowflakeIdGenerator.nextId(); // 这里只是示例，请使用真正的雪花算法或ID生成器
        user.setId(userId);
        user.setUserCode(UUID.randomUUID().toString());
        user.setUsername(registrationRequest.getUsername());
        user.setEmail(registrationRequest.getEmail());
        user.setPassword(encoder.encode(registrationRequest.getPassword()));
        user.setAvatar(registrationRequest.getAvatar()); // 可设置默认头像
        user.setRealName(registrationRequest.getRealName()); // 根据需求设置
        user.setStatus(0); // 正常状态
        user.setIsDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 3. 插入用户记录
        userMapper.insert(user);

        // 4. 关联默认角色（假设默认角色代码为 "USER"）
        SysRole defaultRole = sysRoleMapper.selectByRoleCode("USER");
        if (defaultRole != null) {
            SysUserRole userRole = new SysUserRole();
            // 生成用户角色关联ID（同样使用示例ID）
            userRole.setId(System.currentTimeMillis());
            userRole.setUserId(user.getId());
            userRole.setRoleId(defaultRole.getId());
            userRole.setCreateTime(LocalDateTime.now());
            userRole.setCreatorId(1L); // 默认为管理员可根据实际情况设置
            sysUserRoleMapper.insert(userRole);
        }

        // 8. 构造响应DTO
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());

        return Result.success(response);
    }

    @Override
    @Transactional
    public Result<LoginResponse> login(LoginRequest loginRequest) {
        // 1. 根据邮箱从数据库获取用户信息
        User user = userMapper.selectByEmail(loginRequest.getEmail());
        if (user == null) {
            return Result.error(ResultCode.VALIDATE_FAILED); // 用户不存在返回404
        }

        // 2. 校验密码
        if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return Result.error(ResultCode.VALIDATE_FAILED); // 密码错误返回400
        }

        // 3. 获取用户角色信息
        List<SysUserRole> userRoles = sysUserRoleMapper.selectByUserId(user.getId());
        if (userRoles.isEmpty()) {
            return Result.error(ResultCode.ERROR); // 角色未分配错误
        }

        // 4. 构造 UserRoleWrapper 对象
        UserRoleWrapper userRoleWrapper = new UserRoleWrapper();
        userRoleWrapper.setUserId(user.getId());
        userRoleWrapper.setUsername(user.getUsername());
        userRoleWrapper.setEmail(user.getEmail());
        userRoleWrapper.setStatus(user.getStatus());

        // 设置用户角色信息
        userRoleWrapper.setRoles(userRoles.stream().map(role -> new UserRoleWrapper.RoleInfo(
                role.getRoleId(), sysRoleMapper.selectById(role.getRoleId()).getRoleName(), sysRoleMapper.selectById(role.getRoleId()).getRoleCode())).toList());

        // 设置 JWT 签发和过期时间
        userRoleWrapper.setIssuedAt(java.time.LocalDateTime.now());
        userRoleWrapper.setExpiration(userRoleWrapper.getIssuedAt().plusHours(1)); // 设置1小时过期

        // 5. 将 UserRoleWrapper 缓存到 Redis
        redisUtil.set("userRoleWrapper:" + user.getId(), userRoleWrapper, 3600); // 缓存1小时

        // 6. 生成 JWT
        String token = jwtUtil.generateToken(userRoleWrapper);

        // 7. 将 JWT 缓存到 Redis
        redisUtil.set("JWT:" + user.getId(), token, 3600); // 缓存1小时

        // 8. 返回响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());

        return Result.success(response);
    }


    @Override
    public Result<Void> logout(String token) {
        // 解析 token 获取用户 ID
        var claims = jwtUtil.parseToken(token.replace("Bearer ", ""));
        if (claims != null) {
            Long userId = claims.get("userId", Long.class);
            // 删除 Redis 中的 JWT
            redisUtil.delete("JWT:" + userId);
            // 删除 Redis 中的 UserRoleWrapper
            redisUtil.delete("userRoleWrapper:" + userId);
        }
        return Result.success();
    }
}
