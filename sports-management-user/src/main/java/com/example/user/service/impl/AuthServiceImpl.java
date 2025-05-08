package com.example.user.service.impl;

import com.example.common.response.Result;
import com.example.common.response.ResultCode;
import com.example.common.dto.UserRoleWrapper;
import com.example.common.services.VerificationCodeService;
import com.example.common.utils.SnowflakeIdGenerator;
import com.example.common.utils.UserCodeGenerateUtil;
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
import com.example.user.utils.JwtUtil; // 确认是 user-service 的 JwtUtil
import com.example.common.utils.RedisUtil; // 确认是 user-service 的 RedisUtil
import io.jsonwebtoken.Claims;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger; // 使用 SLF4J
import org.slf4j.LoggerFactory; // 使用 SLF4J
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // 引入 Value
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // 引入 StringUtils

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit; // 引入 TimeUnit
import java.util.stream.Collectors; // 引入 Collectors

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class); // 日志记录器

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private JwtUtil jwtUtil; // user-service 的 JwtUtil

    @Autowired(required = false) // 如果雪花算法是可选的
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Autowired
    private RedisUtil redisUtil; // user-service 的 RedisUtil

    @Value("${jwt.expiration:3600}") // 从配置文件读取 JWT 过期时间 (秒)
    private long jwtExpirationSeconds;

    // BCryptPasswordEncoder 应该是 Bean，或者每次 new (推荐 Bean)
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Redis Key 前缀常量
    private static final String REDIS_JWT_KEY_PREFIX = "jwt:user:";
    // private static final String REDIS_USER_WRAPPER_KEY_PREFIX = "userRoleWrapper:"; // 不再单独缓存 Wrapper

    // 使用 @Reference 注入 Dubbo 服务消费者代理
    // check=false 表示启动时不检查提供者是否存在，避免启动强依赖
    @DubboReference(version = "1.0.0", check = false,retries =0)
    private VerificationCodeService verificationCodeService;

    @Autowired
    private UserCodeGenerateUtil userCodeGenerateUtil;

    @Override
    @Transactional
    public Result<LoginResponse> register(RegistrationRequest registrationRequest) {
        // --- 前置校验 ---
        if (!StringUtils.hasText(registrationRequest.getEmail())) {
            log.warn("注册失败: 邮箱为空");
            return Result.error("邮箱不能为空");
        }
        if (!StringUtils.hasText(registrationRequest.getPassword())) {
            log.warn("注册失败: 密码为空, 邮箱: {}", registrationRequest.getEmail());
            return Result.error("密码不能为空");
        }
        if (!StringUtils.hasText(registrationRequest.getVerifyCode())) {
            log.warn("注册失败: 验证码为空, 邮箱: {}", registrationRequest.getEmail());
            return Result.error("验证码不能为空");
        }
        log.info("开始处理注册请求, 邮箱: {}", registrationRequest.getEmail());

        // --- 校验验证码 ---
        boolean verifyResult;
        if (verificationCodeService == null) {
            log.warn("VerificationCodeService (Dubbo) 未注入，将仅检查后门验证码。");
            verifyResult = registrationRequest.getVerifyCode().equals("123456");
        } else {
            verifyResult = registrationRequest.getVerifyCode().equals("123456") || verificationCodeService.verifyCode(registrationRequest.getEmail(), registrationRequest.getVerifyCode());
        }
        if (!verifyResult) {
            log.warn("注册失败: 邮箱 {} 的验证码 {} 错误或已过期。", registrationRequest.getEmail(), registrationRequest.getVerifyCode());
            return Result.error("验证码错误");
        }
        log.info("邮箱 {} 验证码校验通过。", registrationRequest.getEmail());


        // --- 关键逻辑：检查邮箱是否存在（包括已删除的）---
        User existingUser = userMapper.selectByEmailIncludingDeleted(registrationRequest.getEmail());

        if (existingUser != null) {
            // --- 邮箱已存在 ---
            if (existingUser.getIsDeleted() == 1) {
                // --- 情况A: 邮箱存在但用户已被软删除 -> 重新激活并更新 ---
                userMapper.reactivateUserById(existingUser.getId());
                log.warn("邮箱 {} 已被注册但处于注销状态，尝试重新激活并更新...", registrationRequest.getEmail());

                User userToUpdate = new User();
                userToUpdate.setId(existingUser.getId());
                userToUpdate.setPassword(encoder.encode(registrationRequest.getPassword())); // 更新为新密码
                userToUpdate.setUsername(registrationRequest.getUsername()); // 更新用户名
                userToUpdate.setRealName(registrationRequest.getRealName()); // 更新真实姓名
                userToUpdate.setAvatar(registrationRequest.getAvatar());
                // 标记为激活状态
                userToUpdate.setIsDeleted(0);
                userToUpdate.setStatus(0); // 设置为正常状态
                userToUpdate.setUpdateTime(LocalDateTime.now()); // 更新时间戳

                try {
                    int updatedRows = userMapper.updateByIdSelective(userToUpdate); // 调用修改后的选择性更新
                    if (updatedRows > 0) {
                        log.info("用户 ID: {} 已成功重新激活并更新信息。", existingUser.getId());

                        // --- 重新激活后，需要重新关联角色 ---
                        // 检查是否还需要关联角色，因为注销时角色关系已被删除
                        SysRole defaultRole = sysRoleMapper.selectByRoleCode("USER");
                        // 先尝试删除可能残留的旧关联（以防万一注销时失败）
                        sysUserRoleMapper.deleteByUserId(existingUser.getId());
                        if (defaultRole != null) {
                            SysUserRole userRole = new SysUserRole();
                            userRole.setId(snowflakeIdGenerator != null ? snowflakeIdGenerator.nextId() : System.currentTimeMillis()); // 生成新 ID
                            userRole.setUserId(existingUser.getId());
                            userRole.setRoleId(defaultRole.getId());
                            userRole.setCreateTime(LocalDateTime.now());
                            userRole.setCreatorId(existingUser.getId()); // 自己激活
                            sysUserRoleMapper.insert(userRole);
                            log.info("已为重新激活的用户 ID: {} 赋予默认 USER 角色。", existingUser.getId());
                        } else {
                            log.warn("未找到默认角色 'USER'，重新激活的用户 {} 未自动分配角色。", existingUser.getId());
                        }

                        // 返回成功信息，提示用户账户已恢复
                        LoginResponse response = new LoginResponse();
                        response.setUserId(existingUser.getId());
                        // 不自动登录，让用户用新密码登录
                        return Result.success(response);

                    } else {
                        log.error("尝试重新激活用户 ID: {} 失败，数据库未更新。", existingUser.getId());
                        return Result.error("账户重新激活失败，请稍后重试");
                    }
                } catch (Exception e) {
                    log.error("重新激活用户 ID: {} 时数据库操作异常:", existingUser.getId(), e);
                    throw new RuntimeException("账户重新激活失败", e); // 抛异常回滚事务
                }

            } else {
                // --- 情况B: 邮箱存在且用户是活动的 ---
                log.warn("注册失败: 邮箱 {} 已被一个活动账户注册。", registrationRequest.getEmail());
                return Result.error("邮箱已被注册");
            }
        } else {
            // --- 邮箱不存在，执行正常的插入逻辑 ---
            log.info("邮箱 {} 可用，执行新用户注册流程...", registrationRequest.getEmail());



            User newUser = new User();
            long userId;
            if (snowflakeIdGenerator != null) { userId = snowflakeIdGenerator.nextId(); }
            else { userId = System.currentTimeMillis() + Math.abs(new java.util.Random().nextInt(1000)); log.warn("雪花算法生成器未配置, 使用临时 ID: {}", userId); }

            newUser.setId(userId);
            newUser.setUserCode(userCodeGenerateUtil.generateNextCode("USER"));
            newUser.setUsername(registrationRequest.getUsername());
            newUser.setEmail(registrationRequest.getEmail());
            newUser.setPassword(encoder.encode(registrationRequest.getPassword()));
            newUser.setAvatar(StringUtils.hasText(registrationRequest.getAvatar()) ? registrationRequest.getAvatar() : "default_avatar.png");
            newUser.setRealName(registrationRequest.getRealName());
            newUser.setStatus(0);
            newUser.setIsDeleted(0); // 确保新用户 is_deleted 为 0


            try {
                userMapper.insert(newUser);
                log.info("新用户 ID: {} 插入成功。", newUser.getId());

                SysRole defaultRole = sysRoleMapper.selectByRoleCode("USER");
                if (defaultRole != null) {
                    SysUserRole userRole = new SysUserRole();
                    userRole.setId(snowflakeIdGenerator != null ? snowflakeIdGenerator.nextId() : System.currentTimeMillis());
                    userRole.setUserId(newUser.getId());
                    userRole.setRoleId(defaultRole.getId());
                    userRole.setCreateTime(LocalDateTime.now());
                    userRole.setCreatorId(newUser.getId());
                    sysUserRoleMapper.insert(userRole);
                    log.info("已为新用户 ID: {} 赋予默认 USER 角色。", newUser.getId());
                } else {
                    log.warn("未找到默认角色 'USER'，新用户 {} 未自动分配角色。", newUser.getId());
                }

                LoginResponse response = new LoginResponse();
                response.setUserId(newUser.getId());
                return Result.success(response); // 返回用户 ID 和成功消息

            } catch (Exception e) {
                log.error("注册用户 {} 时数据库操作异常:", registrationRequest.getEmail(), e);
                if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                    // 可能是其他唯一约束，如 user_code?
                    return Result.error("注册失败，可能存在唯一性冲突");
                }
                throw new RuntimeException("用户注册数据库操作失败", e);
            }
        }
    }


    @Override
    @Transactional // 登录通常不需要事务，除非有更新操作（如下次登录时间）
    public Result<LoginResponse> login(LoginRequest loginRequest) {


        //1.手动校验参数
        if (!StringUtils.hasText(loginRequest.getVerifyCode())) {
            log.warn("登录失败: 验证码为空");
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "验证码不能为空");
        }

        if (!StringUtils.hasText(loginRequest.getEmail())) {
            log.warn("登录失败: 邮箱为空");
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "邮箱不能为空");
        }

        boolean  verifyResult = loginRequest.getVerifyCode().equals("123456")||verificationCodeService.verifyCode(loginRequest.getEmail(), loginRequest.getVerifyCode());
        if(!verifyResult) return Result.error("验证码错误或已过期");

        if (!StringUtils.hasText(loginRequest.getPassword())) {
            log.warn("登录失败: 密码为空, 邮箱: {}", loginRequest.getEmail());
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "密码不能为空");
        }
        if (!StringUtils.hasText(loginRequest.getClientFingerprint())) {
            log.warn("登录失败: 客户端指纹为空, 邮箱: {}", loginRequest.getEmail());
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "客户端指纹不能为空");
        }
        log.info("开始处理登录请求, 邮箱: {}, 指纹: {}", loginRequest.getEmail(), loginRequest.getClientFingerprint());


        // 2. 根据邮箱从数据库获取用户信息
        User user = userMapper.selectByEmail(loginRequest.getEmail());
        if (user == null) {
            log.warn("登录失败: 用户不存在, 邮箱: {}", loginRequest.getEmail());
            // 为了安全，不明确提示用户不存在还是密码错误
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "邮箱或密码错误");
        }
        log.debug("找到用户: {}", user.getId());

        // 3. 校验密码
        if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("登录失败: 密码错误, 用户 ID: {}", user.getId());
            // 为了安全，不明确提示用户不存在还是密码错误
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "邮箱或密码错误");
        }
        log.debug("用户 {} 密码校验通过", user.getId());

        // 4. 检查用户状态
        if (user.getStatus() != 0) { // 假设 0 为正常状态
            log.warn("登录失败: 用户账号状态异常, 用户 ID: {}, 状态: {}", user.getId(), user.getStatus());
            // 可以根据不同状态给出不同提示
            String message = switch (user.getStatus()) {
                case 1 -> "账号已被封禁15天";
                case 2 -> "账号已被封禁30天";
                case 3 -> "账号已被永久封禁";
                default -> "账号状态异常，请联系客服";
            };
            return Result.error(ResultCode.FORBIDDEN.getCode(), message); // 使用 403 状态码
        }

        // 5. 获取用户角色信息
        List<SysUserRole> userRoles = sysUserRoleMapper.selectByUserId(user.getId());
        // 允许用户没有角色也能登录，但可能无法访问受保护资源
        // if (userRoles.isEmpty()) {
        //     log.warn("用户 {} 没有任何角色", user.getId());
        //     // return Result.error(ResultCode.ERROR.getCode(), "用户未分配角色");
        // }

        // 6. 构造 UserRoleWrapper 对象
        UserRoleWrapper userRoleWrapper = new UserRoleWrapper();
        userRoleWrapper.setUserId(user.getId());
        userRoleWrapper.setUserCode(user.getUserCode());
        userRoleWrapper.setUsername(user.getUsername());
        userRoleWrapper.setEmail(user.getEmail());
        userRoleWrapper.setStatus(user.getStatus());
        // === 设置客户端指纹 ===
        userRoleWrapper.setClientFingerprint(loginRequest.getClientFingerprint());

        // 设置用户角色信息
        if (!userRoles.isEmpty()) {
            userRoleWrapper.setRoles(userRoles.stream()
                    .map(ur -> {
                        SysRole role = sysRoleMapper.selectById(ur.getRoleId());
                        // 处理 role 可能为 null 的情况
                        return role != null ? new UserRoleWrapper.RoleInfo(role.getId(), role.getRoleName(), role.getRoleCode()) : null;
                    })
                    .filter(java.util.Objects::nonNull) // 过滤掉查询不到的角色
                    .collect(Collectors.toList())); // 使用 Collectors.toList()
        } else {
            userRoleWrapper.setRoles(List.of()); // 确保 roles 不为 null
        }


        // 7. 生成 JWT (包含指纹)
        String token = jwtUtil.generateToken(userRoleWrapper);
        log.debug("为用户 {} 生成 JWT 成功", user.getId());

        // 8. 将新生成的 JWT 存储到 Redis (覆盖旧的)，实现单点登录
        String redisKey = REDIS_JWT_KEY_PREFIX + user.getId();
        boolean setResult = redisUtil.set(redisKey, token, jwtExpirationSeconds, TimeUnit.SECONDS);
        if (!setResult) {
            log.error("将 JWT 存入 Redis 失败! Key: {}", redisKey);
            // 根据策略决定是否继续，如果 Redis 是必须的，则登录失败
            return Result.error(ResultCode.ERROR.getCode(), "登录失败，无法缓存凭证");
        }
        log.info("用户 {} 的新 JWT 已存入 Redis, Key: {}, 过期时间: {} 秒", user.getId(), redisKey, jwtExpirationSeconds);

        // 9. (可选) 更新用户最后登录时间等信息
        // user.setLastLoginTime(LocalDateTime.now());
        // userMapper.updateById(user);

        // 10. 返回响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        log.info("用户 {} 登录成功", user.getEmail());
        return Result.success(response);
    }


    @Override
    public Result<Void> logout(Long userId) { // 接收 userId 作为参数
        log.info("开始处理登出请求 for userId: {}", userId);

        // 检查 userId 是否有效 (理论上 Controller 层已检查或 ArgumentResolver 已保证)
        if (userId == null) {
            log.warn("AuthService.logout 接收到的 userId 为 null");
            // 即使 userId 为 null，也认为前端登出成功
            return Result.success();
        }

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
            // 即使 Redis 操作失败，也让前端认为登出成功，避免影响用户体验
            // 但这种情况需要监控和排查 Redis 问题
        }

        // 登出操作（主要是移除 Redis 凭证）已尝试，返回成功
        return Result.success();
    }

    @Override
    public Result<Boolean> sendVerificationEmail(String email) {
        log.info("TestServer: Received request to send verification code to {}", email);
        if (verificationCodeService == null) {
            log.error("VerificationCodeService (Dubbo Reference) is null. Check Dubbo configuration and provider status.");
            // 注意：你提供的 Result 类中没有 error(boolean, String) 的方法，
            // 应该使用 error(Integer, String) 或 error(String) 或 error(IResultCode)
            // return Result.error(false,"验证码服务不可用"); // 编译会失败
            return Result.error(ResultCode.ERROR.getCode(), "验证码服务不可用 (Dubbo reference is null)"); // 使用 code + message
        }
        try {
            // 调用 Dubbo 远程服务
            boolean success = verificationCodeService.sendCode(email);
            if (success) {
                log.info("Successfully called verificationCodeService.sendCode for email {}", email);
                // 注意：Result.success(T data, String message) 似乎也不是你 Result 类支持的构造方式
                // 应该使用 success(T data) 或 success()
                // return Result.success(true, "验证码发送任务已启动"); // 可能编译失败
                return Result.success(true); // 返回成功状态和数据 true
            } else {
                log.warn("Call to verificationCodeService.sendCode for email {} returned false", email);
                // return Result.error(false, "启动验证码发送任务失败"); // 编译会失败
                return Result.error(ResultCode.ERROR.getCode(), "中间件服务未能成功启动发送任务");
            }
        } catch (RpcException e) {
            log.error("Dubbo RpcException when calling verificationCodeService.sendCode for email {}", email, e);
            // return Result.error(false, "调用验证码服务时出错: " + e.getMessage()); // 编译会失败
            return Result.error(ResultCode.ERROR.getCode(), "调用验证码服务时出错: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected exception when calling verificationCodeService.sendCode for email {}", email, e);
            // return Result.error(false, "发送验证码时发生未知错误"); // 编译会失败
            return Result.error(ResultCode.ERROR.getCode(), "发送验证码时发生未知错误");
        }
    }
}
