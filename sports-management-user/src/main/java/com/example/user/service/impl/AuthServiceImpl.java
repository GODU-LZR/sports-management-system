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
import com.example.user.utils.JwtUtil; // 确认是 user-service 的 JwtUtil
import com.example.user.utils.RedisUtil; // 确认是 user-service 的 RedisUtil
import io.jsonwebtoken.Claims;
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

    @Override
    @Transactional
    public Result<LoginResponse> register(RegistrationRequest registrationRequest) {
        log.info("开始处理注册请求, 邮箱: {}", registrationRequest.getEmail());
        // 1. 检查邮箱是否已被注册
        // 注意：userService.getUserByEmail 可能需要调整以适应 Result 包装
        User existingUser = userMapper.selectByEmail(registrationRequest.getEmail());
        if (existingUser != null) {
            log.warn("注册失败: 邮箱 {} 已被注册", registrationRequest.getEmail());
            return Result.error("邮箱已被注册");
        }

        // 2. 构造 User 对象
        User user = new User();
        long userId;
        if (snowflakeIdGenerator != null) {
            userId = snowflakeIdGenerator.nextId();
            log.debug("使用雪花算法生成用户 ID: {}", userId);
        } else {
            // 备选方案：如果雪花算法不可用，可以使用 UUID 或其他方式，但 Long 类型可能不兼容
            // 这里暂时用时间戳 + 随机数代替，**强烈建议配置好雪花算法**
            userId = System.currentTimeMillis() + Math.abs(new java.util.Random().nextInt(1000));
            log.warn("雪花算法生成器未配置, 使用临时 ID: {}", userId);
        }
        user.setId(userId);
        user.setUserCode(UUID.randomUUID().toString()); // 唯一用户代码
        user.setUsername(registrationRequest.getUsername());
        user.setEmail(registrationRequest.getEmail());
        user.setPassword(encoder.encode(registrationRequest.getPassword())); // 加密存储密码
        user.setAvatar(StringUtils.hasText(registrationRequest.getAvatar()) ? registrationRequest.getAvatar() : "default_avatar.png"); // 设置默认头像
        user.setRealName(registrationRequest.getRealName());
        user.setStatus(0); // 0-正常
        user.setIsDeleted(0); // 0-未删除
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);

        // 3. 插入用户记录
        int userInserted = userMapper.insert(user);
        if (userInserted <= 0) {
            log.error("插入用户记录失败: {}", user);
            // 手动回滚事务（如果需要，但 @Transactional 应该会自动处理）
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.error("用户注册失败，请稍后重试");
        }
        log.info("用户记录插入成功, 用户 ID: {}", user.getId());

        // 4. 关联默认角色 (假设默认角色代码为 "USER")
        SysRole defaultRole = sysRoleMapper.selectByRoleCode("USER");
        if (defaultRole != null) {
            SysUserRole userRole = new SysUserRole();
            // 用户角色关联表 ID 也建议用 ID 生成器
            long userRoleId;
            if (snowflakeIdGenerator != null) {
                userRoleId = snowflakeIdGenerator.nextId();
            } else {
                userRoleId = System.currentTimeMillis() + Math.abs(new java.util.Random().nextInt(1000));
            }
            userRole.setId(userRoleId);
            userRole.setUserId(user.getId());
            userRole.setRoleId(defaultRole.getId());
            userRole.setCreateTime(now);
            userRole.setCreatorId(user.getId()); // 创建者设置为用户自己
            int roleInserted = sysUserRoleMapper.insert(userRole);
            if (roleInserted <= 0) {
                log.error("插入用户角色关联记录失败: {}", userRole);
                // 手动回滚事务
                // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return Result.error("用户角色分配失败，请联系管理员");
            }
            log.info("用户 {} 关联默认角色 {} 成功", user.getId(), defaultRole.getRoleCode());
        } else {
            log.warn("未找到默认角色 'USER', 用户 {} 未分配任何角色", user.getId());
            // 根据业务决定是否允许无角色用户注册
            // return Result.error("系统错误：无法分配默认角色");
        }

        // 5. 注册成功，直接返回成功信息，不自动登录（如果需要自动登录，则调用 login 逻辑）
        log.info("用户 {} 注册成功", user.getEmail());
        // 如果需要注册后自动登录，取消下面的注释并调用 login 逻辑
        /*
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail(registrationRequest.getEmail());
        loginReq.setPassword(registrationRequest.getPassword());
        // 注意：注册时前端可能不传递指纹，如果需要自动登录，指纹需要特殊处理或让用户重新登录
        // loginReq.setClientFingerprint("REGISTER_AUTO_LOGIN_PLACEHOLDER"); // 或者不设置，让 login 报错？
        return login(loginReq); // 调用登录逻辑
        */

        // 仅返回注册成功信息
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        // 不返回 token，提示用户去登录
        return Result.success(response); // 返回用户ID，让前端引导用户登录
    }

    @Override
    @Transactional // 登录通常不需要事务，除非有更新操作（如下次登录时间）
    public Result<LoginResponse> login(LoginRequest loginRequest) {

        //1.手动校验参数
        if (!StringUtils.hasText(loginRequest.getEmail())) {
            log.warn("登录失败: 邮箱为空");
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "邮箱不能为空");
        }
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

        // 设置 JWT 签发和过期时间 (使用 JwtUtil 内部逻辑，这里不再设置)
        // userRoleWrapper.setIssuedAt(LocalDateTime.now());
        // userRoleWrapper.setExpiration(userRoleWrapper.getIssuedAt().plusSeconds(jwtExpirationSeconds));

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
    public Result<Void> logout(String authorizationHeader) {
        log.info("开始处理登出请求");
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("登出失败: 无效的 Authorization header");
            // 即使 header 无效，也返回成功，因为客户端已经认为自己登出了
            return Result.success();
        }

        String token = authorizationHeader.substring(7); // 提取 token

        // 解析 token 获取用户 ID
        Claims claims = jwtUtil.parseToken(token); // 使用 user-service 的 JwtUtil 解析
        if (claims != null) {
            Long userId = jwtUtil.getClaimFromToken(claims, JwtUtil.CLAIM_USER_ID, Long.class);
            if (userId != null) {
                // 删除 Redis 中的 JWT
                String redisKey = REDIS_JWT_KEY_PREFIX + userId;
                boolean deleted = redisUtil.delete(redisKey);
                if (deleted) {
                    log.info("用户 {} 的 JWT 已从 Redis 删除, Key: {}", userId, redisKey);
                } else {
                    // Key 可能已过期或不存在，也算登出成功
                    log.warn("尝试删除 Redis 中的 JWT 时 Key 不存在或删除失败, Key: {}", redisKey);
                }
            } else {
                log.warn("从 token 中未能解析出有效的 userId");
            }
        } else {
            log.warn("登出请求中的 token 无效或已过期");
            // Token 无效也视为登出成功
        }

        // 无论 Redis 操作是否成功，都返回成功，让前端完成登出流程
        return Result.success();
    }
}
