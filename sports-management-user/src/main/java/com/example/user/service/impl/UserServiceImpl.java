package com.example.user.service.impl;

import com.example.common.response.Result;
import com.example.common.response.ResultCode;
import com.example.common.services.VerificationCodeService;
import com.example.common.utils.SnowflakeIdGenerator;
import com.example.common.utils.UserCodeGenerateUtil;
import com.example.user.dto.UserProfileDTO;
import com.example.user.mapper.SysRoleMapper;
import com.example.user.mapper.SysUserRoleMapper;
import com.example.user.mapper.UserMapper;
import com.example.user.pojo.SysRole;
import com.example.user.pojo.SysUserRole;
import com.example.user.pojo.User;
import com.example.user.service.UserService;
import com.example.common.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
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
    // 注入新的 UserCodeGenerateUtil，变量名也建议修改
    @Autowired
    private UserCodeGenerateUtil userCodeGenerateUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // 使用 @Reference 注入 Dubbo 服务消费者代理
    // check=false 表示启动时不检查提供者是否存在，避免启动强依赖
    @DubboReference(version = "1.0.0", check = false,retries =0)
    private VerificationCodeService verificationCodeService;

    // Redis Key 前缀常量
    private static final String REDIS_JWT_KEY_PREFIX = "jwt:user:";



    @Override
    @Transactional
    public Result<User> createUser(User user) {
        // 生成ID和UserCode
        long userId = snowflakeIdGenerator.nextId();
        user.setId(userId);
        user.setUserCode(userCodeGenerateUtil.generateNextCode("USER"));
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
        userMapper.deleteByIdPhysical(id);
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

    // --- 新增获取用户 Profile 的实现 ---
    @Override
    public Result<UserProfileDTO> getUserProfileById(Long userId) {
        // 1. 获取用户基本信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("尝试获取用户 Profile 失败，用户不存在: ID={}", userId);
            return Result.error(ResultCode.VALIDATE_FAILED);
        }

        // 2. 获取用户的角色 ID 列表
        List<Long> roleIds;
        try {
            List<SysUserRole> userRoles = sysUserRoleMapper.selectByUserId(userId);
            if (userRoles != null && !userRoles.isEmpty()) {
                roleIds = userRoles.stream()
                        .map(SysUserRole::getRoleId)
                        .distinct()
                        .collect(Collectors.toList());
                log.info("通过 Mapper 获取用户 ID: {} 关联的角色 ID 列表: {}", userId, roleIds);
            } else {
                log.info("用户 ID: {} 没有关联的角色记录", userId);
                roleIds = Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("使用 SysUserRoleMapper 获取用户 ID: {} 的角色关联时发生异常", userId, e);
            return Result.error(ResultCode.VALIDATE_FAILED);
        }

        // 3. 根据角色 ID 列表获取角色代码
        List<String> roleCodes = Collections.emptyList();
        if (!roleIds.isEmpty()) {
            try {
                // **重要**: 假设 SysRoleMapper 存在 selectBatchIds 方法
                // List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);

                // 如果 SysRoleMapper 没有 selectBatchIds，则使用 selectById 循环查询 (性能较低)
                List<SysRole> roles = roleIds.stream()
                        .map(roleId -> {
                            try {
                                // 假设 SysRoleMapper 有 selectById 方法
                                return sysRoleMapper.selectById(roleId);
                            } catch (Exception roleEx) {
                                log.error("查询单个角色 ID: {} 时异常", roleId, roleEx);
                                return null; // 单个查询失败不影响整体
                            }
                        })
                        .filter(Objects::nonNull) // 过滤掉查询失败或不存在的角色
                        .collect(Collectors.toList());


                if (!roles.isEmpty()) {
                    roleCodes = roles.stream()
                            .map(SysRole::getRoleCode)
                            .filter(Objects::nonNull) // 确保 roleCode 不为 null
                            .collect(Collectors.toList());
                    log.info("通过 Mapper 获取用户 ID: {} 对应的角色代码列表: {}", userId, roleCodes);
                } else {
                    log.warn("根据角色 ID 列表 {} 未查询到有效的角色信息", roleIds);
                }
            } catch (Exception e) {
                log.error("使用 SysRoleMapper 根据角色 ID 列表 {} 获取角色信息时发生异常", roleIds, e);
                // 这里可以选择是返回部分信息还是报错，报错更安全
                return Result.error(ResultCode.VALIDATE_FAILED);
            }
        }

        // 4. 组装 DTO
        UserProfileDTO userProfileDTO = UserProfileDTO.fromUser(user, roleCodes);
        log.info("成功构建用户 ID: {} 的 Profile DTO (Mapper 版本)", userId);
        return Result.success(userProfileDTO);
    }

    // --- 实现更新当前用户 Profile 的方法 ---
    @Override
    @Transactional
    public Result<UserProfileDTO> updateCurrentUserProfile(Long userId, Map<String, Object> updateData) {
        // 1. 获取当前用户数据
        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            return Result.error("用户不存在");
        }

        User userToUpdate = new User(); // 用于选择性更新
        userToUpdate.setId(userId);
        boolean needsDbUpdate = false;

        // --- 2. 处理密码更新 ---
        if (updateData.containsKey("password")) {
            String newPassword = (String) updateData.get("password");
            if (StringUtils.hasText(newPassword)) {
                String oldPassword = (String) updateData.get("oldPassword");
                if (!StringUtils.hasText(oldPassword)) {
                    return Result.error("修改密码需提供原密码");
                }
                if (!passwordEncoder.matches(oldPassword, existingUser.getPassword())) {
                    return Result.error("原密码不正确");
                }
                userToUpdate.setPassword(passwordEncoder.encode(newPassword));
                needsDbUpdate = true;
            }
        }

        // --- 3. 处理邮箱更新 ---
        if (updateData.containsKey("email")) {
            String newEmail = (String) updateData.get("email");
            if (newEmail != null && !newEmail.equals(existingUser.getEmail())) {
                String emailCode = (String) updateData.get("emailCode");
                if (!StringUtils.hasText(emailCode)) {
                    return Result.error("修改邮箱需要提供验证码");
                }
                // --- 使用 VerificationCodeService 进行校验 (包含后门) ---
                boolean verifyResult = emailCode.equals("123456") || verificationCodeService.verifyCode(newEmail, emailCode);
                if (!verifyResult) {
                    return Result.error("验证码错误或已过期");
                }
                // --- 验证通过 ---
                userToUpdate.setEmail(newEmail);
                needsDbUpdate = true;
            }
        }

        // --- 4. 处理头像更新 (直接使用传入的 URL) ---
        if (updateData.containsKey("avatar")) {
            String newAvatarUrl = (String) updateData.get("avatar");
            // 只有当传入了非 null 的 URL，且与现有 URL 不同时才更新
            if (newAvatarUrl != null && !newAvatarUrl.equals(existingUser.getAvatar())) {
                userToUpdate.setAvatar(newAvatarUrl);
                needsDbUpdate = true;
            }
        }

        // --- 5. 处理其他允许更新的字段 ---
        if (updateData.containsKey("username") && !Objects.equals(updateData.get("username"), existingUser.getUsername())) {
            userToUpdate.setUsername((String) updateData.get("username"));
            needsDbUpdate = true;
        }
        if (updateData.containsKey("realName") && !Objects.equals(updateData.get("realName"), existingUser.getRealName())) {
            userToUpdate.setRealName((String) updateData.get("realName"));
            needsDbUpdate = true;
        }

        // --- 6. 执行数据库更新 ---
        if (needsDbUpdate) {
            userToUpdate.setUpdateTime(LocalDateTime.now());
            try {
                int updatedRows = userMapper.updateByIdSelective(userToUpdate);
                if (updatedRows == 0) {
                    log.warn("更新用户信息数据库操作未影响任何行，用户ID: {}", userId);
                    return Result.error("更新失败，请稍后重试");
                }
                log.info("用户 {} 信息已更新成功", userId);
            } catch (Exception e) {
                log.error("更新用户 {} 信息时数据库异常", userId, e);
                throw new RuntimeException("更新用户信息数据库操作失败", e); // 抛异常以回滚
            }
        } else {
            log.info("用户 {} 提交的信息无变化，无需更新数据库", userId);
        }

        // --- 7. 返回最新的 UserProfileDTO ---
        return this.getUserProfileById(userId);
    }

    // --- 修改后的注销方法实现 ---
    @Override
    @Transactional // 确保数据库操作的原子性
    public Result<Boolean> deactivateCurrentUser(Long userId, String emailCode, String password) { // 添加 password 参数
        log.info("用户 ID: {} 尝试注销账户，提供的验证码: {}, 密码: [PROTECTED]", userId, emailCode); // 不要在日志中打印明文密码

        // 1. 检查用户是否存在
        User currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            log.warn("注销失败：用户 ID: {} 不存在或已被删除。", userId);
            return Result.error("用户不存在");
        }

        // --- 新增：2. 校验用户密码 ---
        if (!StringUtils.hasText(password)) {
            log.warn("注销失败：用户 ID: {} 未提供密码。", userId);
            return Result.error("请输入当前密码");
        }
        // 使用 PasswordEncoder 校验密码
        if (!passwordEncoder.matches(password, currentUser.getPassword())) {
            log.warn("注销失败：用户 ID: {} 提供的密码不正确。", userId);
            // 可以考虑添加尝试次数限制等安全措施
            return Result.error("密码错误"); // 使用合适的密码错误码
        }
        log.info("用户 ID: {} 密码校验通过。", userId);

        // --- 3. 校验邮箱验证码 --- (原步骤 2)
        String userEmail = currentUser.getEmail();
        if (!StringUtils.hasText(userEmail)) {
            log.error("注销失败：用户 ID: {} 没有有效的邮箱地址，无法进行验证码校验。", userId);
            return Result.error("用户未绑定邮箱，无法注销");
        }
        if (!StringUtils.hasText(emailCode)) {
            log.warn("注销失败：用户 ID: {} 未提供邮箱验证码。", userId);
            return Result.error("请输入邮箱验证码");
        }

        // 调用 VerificationCodeService 进行验证 (包含你的后门 "123456")
        boolean verified;
        if (verificationCodeService == null) {
            log.warn("VerificationCodeService (Dubbo) 未注入或不可用，将仅检查后门验证码。");
            verified = emailCode.equals("123456");
        } else {
            verified = emailCode.equals("123456") || verificationCodeService.verifyCode(userEmail, emailCode);
        }

        if (!verified) {
            log.warn("注销失败：用户 ID: {} 提供的邮箱验证码 {} 错误或已过期。", userId, emailCode);
            return Result.error("验证码过期或错误");
        }
        log.info("用户 ID: {} 邮箱验证码校验通过。", userId);

        // --- 4. 执行数据库操作 --- (原步骤 3)
        try {
            // 4.1 软删除用户表记录
            int userUpdatedRows = userMapper.deactivateUserById(userId);
            if (userUpdatedRows == 0) {
                log.warn("注销操作：更新用户表 is_deleted 标志时影响行数为 0，用户 ID: {}", userId);
            } else {
                log.info("用户 ID: {} 的 is_deleted 标志已更新为 1。", userId);
            }

            // 4.2 删除用户角色关联表记录
            int rolesDeletedCount = sysUserRoleMapper.deleteByUserId(userId);
            log.info("为用户 ID: {} 删除了 {} 条用户角色关联记录。", userId, rolesDeletedCount);

            // 直接使用 userId 删除 Redis 中的 JWT
            String redisKey = REDIS_JWT_KEY_PREFIX + userId;
            try {
                boolean deleted = redisUtil.delete(redisKey);
                if (deleted) {
                    log.info("用户 {} 的 JWT 已从 Redis 删除, Key: {}", userId, redisKey);
                } else {
                    // Key 可能已过期或不存在，也算登出成功
                    log.warn("尝试删除 Redis 中的 JWT 时 Key 不存在或删除失败, Key: {}", redisKey);
                }
            } catch (Exception e) {
                log.error("删除 Redis Key {} 时发生异常", redisKey, e);
            }

            log.info("用户 ID: {} 注销成功。", userId);
            return Result.success(true); // 返回成功

        } catch (Exception e) {
            log.error("用户 ID: {} 注销过程中数据库操作异常:", userId, e);
            throw new RuntimeException("用户注销数据库操作失败", e); // 抛异常以回滚事务
        }
    }


}
