// 在下游微服务中，例如 user-service 的某个包下 (e.g., com.example.userservice.config.resolver)
package com.example.common.config;


import com.example.common.model.RequestHeaders;
import com.example.common.model.UserRoleWrapper; // 导入 common 模块的 UserRoleWrapper
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull; // 确保使用正确的 NonNull/Nullable 注解
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义 Spring MVC 参数解析器
 * 用于从请求头中提取用户信息，并自动注入到 Controller 方法的 UserRoleWrapper 参数中
 */
@Slf4j
@Component // 注册为 Spring Bean，以便配置类可以找到它
public class UserRoleWrapperArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 判断此解析器是否支持当前的 Controller 方法参数
     *
     * @param parameter 方法参数
     * @return 如果参数类型是 UserRoleWrapper，则返回 true
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 检查参数类型是否是我们想要处理的 UserRoleWrapper 类型
        boolean supports = parameter.getParameterType().equals(UserRoleWrapper.class);
        if (supports) {
            log.trace("参数解析器: 支持将请求头解析为 UserRoleWrapper 参数: {}", parameter.getParameterName());
        }
        return supports;
    }

    /**
     * 解析参数的实际方法
     * 当 supportsParameter 返回 true 时，Spring 会调用此方法来获取参数值
     *
     * @param parameter     方法参数 (我们知道它的类型是 UserRoleWrapper)
     * @param mavContainer  ModelAndView 容器 (通常在此场景下用不到)
     * @param webRequest    本地 Web 请求，可以从中获取 HttpServletRequest
     * @param binderFactory 数据绑定工厂 (通常在此场景下用不到)
     * @return 解析后的 UserRoleWrapper 对象，如果必要信息缺失或无效，可能返回 null 或抛出异常 (这里选择返回 null)
     * @throws Exception 如果解析过程中发生不可恢复的错误
     */
    @Override
    @Nullable // 返回值可能是 null，如果头信息不完整或解析失败
    public Object resolveArgument(@NonNull MethodParameter parameter,
                                  @Nullable ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest,
                                  @Nullable WebDataBinderFactory binderFactory) throws Exception {

        // 1. 从 NativeWebRequest 获取 HttpServletRequest
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            log.warn("参数解析器: 无法获取 HttpServletRequest 对象，无法解析用户信息。");
            // 在 WebFlux 环境下这里会是 ServerHttpRequest，需要不同的处理方式
            // 但既然我们基于 Spring MVC，这里应该是能获取到的
            return null; // 或者抛出异常，取决于你的错误处理策略
        }

        log.debug("参数解析器: 开始从请求头解析 UserRoleWrapper, 请求 URI: {}", request.getRequestURI());

        // 2. 从请求头中提取用户信息
        String userIdStr = request.getHeader(RequestHeaders.HEADER_X_USER_ID);
        String username = request.getHeader(RequestHeaders.HEADER_X_USER_USERNAME);
        String email = request.getHeader(RequestHeaders.HEADER_X_USER_EMAIL);
        String statusStr = request.getHeader(RequestHeaders.HEADER_X_USER_STATUS);
        String rolesStr = request.getHeader(RequestHeaders.HEADER_X_USER_ROLES); // 逗号分隔的角色 CODE

        // 3. 校验必要信息是否存在 (至少需要 userId)
        if (!StringUtils.hasText(userIdStr)) {
            log.warn("参数解析器: 请求头中缺少必要的 '{}' 信息，无法构建 UserRoleWrapper。", RequestHeaders.HEADER_X_USER_ID);
            // 根据业务需求决定：
            // 1. 返回 null: Controller 需要处理 null 的情况
            // 2. 抛出异常: 例如 throw new MissingRequestHeaderException(RequestHeaders.HEADER_X_USER_ID, parameter);
            //    这会被 Spring 的异常处理器捕获，通常返回 400 Bad Request
            // 这里选择返回 null，让 Controller 更灵活
            return null;
        }

        UserRoleWrapper user = new UserRoleWrapper();

        // 4. 解析并设置用户信息
        try {
            user.setUserId(Long.parseLong(userIdStr));
        } catch (NumberFormatException e) {
            log.error("参数解析器: 解析用户 ID '{}' 失败: '{}'", RequestHeaders.HEADER_X_USER_ID, userIdStr, e);
            return null; // ID 无效，返回 null
        }

        // 设置可选字段
        user.setUsername(username); // username 可能为 null 或空字符串
        user.setEmail(email);     // email 可能为 null 或空字符串

        // 解析状态
        if (StringUtils.hasText(statusStr)) {
            try {
                user.setStatus(Integer.parseInt(statusStr));
            } catch (NumberFormatException e) {
                log.warn("参数解析器: 解析用户状态 '{}' 失败: '{}'，将忽略此字段。", RequestHeaders.HEADER_X_USER_STATUS, statusStr, e);
                user.setStatus(null); // 或者设置一个默认值
            }
        } else {
            user.setStatus(null); // 如果头中没有，则为 null
        }

        // 解析角色列表 (逗号分隔的角色 CODE)
        if (StringUtils.hasText(rolesStr)) {
            List<UserRoleWrapper.RoleInfo> roles = Arrays.stream(rolesStr.split(","))
                    .map(String::trim) // 去除可能的空格
                    .filter(StringUtils::hasText) // 过滤掉空的角色码
                    .map(roleCode -> {
                        // 注意：这里我们只从 Header 获取了 Role Code
                        // Role ID 和 Role Name 在这个上下文中是未知的
                        // 如果下游需要完整的 RoleInfo，可能需要根据 Role Code 查询数据库或缓存
                        // 这里我们只填充 Code，其他字段为 null
                        UserRoleWrapper.RoleInfo roleInfo = new UserRoleWrapper.RoleInfo();
                        roleInfo.setRoleCode(roleCode);
                        roleInfo.setRoleId(null); // ID 未知
                        roleInfo.setRoleName(null); // Name 未知
                        return roleInfo;
                    })
                    .collect(Collectors.toList());
            user.setRoles(roles);
            log.debug("参数解析器: 解析得到角色列表: {}", roles.stream().map(UserRoleWrapper.RoleInfo::getRoleCode).collect(Collectors.joining(",")));
        } else {
            log.debug("参数解析器: 请求头 '{}' 为空或不存在，用户角色列表为空。", RequestHeaders.HEADER_X_USER_ROLES);
            user.setRoles(Collections.emptyList()); // 如果没有角色头，设置为空列表
        }

        // 注意：UserRoleWrapper 中的 issuedAt, expiration, clientFingerprint 字段
        // 在这个解析器中是无法从请求头获取的 (网关没有传递它们)
        // 如果下游确实需要这些信息，需要调整网关传递的头，或者通过其他方式获取
        user.setIssuedAt(null);
        user.setExpiration(null);
        user.setClientFingerprint(null); // 这个指纹是 JWT 里的，网关校验用，通常不透传

        log.info("参数解析器: 成功从请求头解析 UserRoleWrapper: userId={}, username={}, email={}, status={}, roles={}",
                 user.getUserId(), user.getUsername(), user.getEmail(), user.getStatus(),
                 user.getRoles().stream().map(UserRoleWrapper.RoleInfo::getRoleCode).collect(Collectors.joining(",")));

        // 5. 返回构建好的 UserRoleWrapper 对象
        return user;
    }
}
