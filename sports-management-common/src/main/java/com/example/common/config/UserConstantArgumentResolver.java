package com.example.common.config;

import com.example.common.response.RequestHeaders;
import com.example.common.constant.UserConstant; // 导入 UserConstant 类
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
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
 * 用于从请求头中提取用户信息，并自动注入到 Controller 方法的 UserConstant 参数中
 */
@Slf4j
@Component // 注册为 Spring Bean，以便配置类可以找到它
public class UserConstantArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 判断此解析器是否支持当前的 Controller 方法参数
     *
     * @param parameter 方法参数
     * @return 如果参数类型是 UserConstant，则返回 true
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 检查参数类型是否是我们想要处理的 UserConstant 类型
        boolean supports = parameter.getParameterType().equals(UserConstant.class);
        if (supports) {
            log.trace("参数解析器: 支持将请求头解析为 UserConstant 参数: {}", parameter.getParameterName());
        }
        return supports;
    }

    /**
     * 解析参数的实际方法
     * 当 supportsParameter 返回 true 时，Spring 会调用此方法来获取参数值
     *
     * @param parameter     方法参数 (我们知道它的类型是 UserConstant)
     * @param mavContainer  ModelAndView 容器 (通常在此场景下用不到)
     * @param webRequest    本地 Web 请求，可以从中获取 HttpServletRequest
     * @param binderFactory 数据绑定工厂 (通常在此场景下用不到)
     * @return 解析后的 UserConstant 对象，如果必要信息缺失或无效，可能返回 null 或抛出异常 (这里选择返回 null)
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
            return null; // 或者抛出异常，取决于你的错误处理策略
        }

        log.debug("参数解析器: 开始从请求头解析 UserConstant, 请求 URI: {}", request.getRequestURI());

        // 2. 从请求头中提取用户信息
        String userIdStr = request.getHeader(RequestHeaders.HEADER_X_USER_ID);
        String userCode = request.getHeader(RequestHeaders.HEADER_X_USER_CODE);
        String username = request.getHeader(RequestHeaders.HEADER_X_USER_USERNAME);
        String email = request.getHeader(RequestHeaders.HEADER_X_USER_EMAIL);
        String statusStr = request.getHeader(RequestHeaders.HEADER_X_USER_STATUS);
        String rolesStr = request.getHeader(RequestHeaders.HEADER_X_USER_ROLES); // 逗号分隔的角色 CODE

        // 3. 校验必要信息是否存在 (至少需要 userId)
        if (!StringUtils.hasText(userIdStr)) {
            log.warn("参数解析器: 请求头中缺少必要的 '{}' 信息，无法构建 UserConstant。", RequestHeaders.HEADER_X_USER_ID);
            return null;
        }

        UserConstant user = new UserConstant();

        // 4. 解析并设置用户信息
        try {
            user.setUserId(Long.parseLong(userIdStr));
        } catch (NumberFormatException e) {
            log.error("参数解析器: 解析用户 ID '{}' 失败: '{}'", RequestHeaders.HEADER_X_USER_ID, userIdStr, e);
            return null; // ID 无效，返回 null
        }

        // 设置可选字段
        user.setUserCode(userCode);
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
            List<UserConstant.RoleInfo> roles = Arrays.stream(rolesStr.split(","))
                    .map(String::trim) // 去除可能的空格
                    .filter(StringUtils::hasText) // 过滤掉空的角色码
                    .map(roleCode -> {
                        UserConstant.RoleInfo roleInfo = new UserConstant.RoleInfo();
                        roleInfo.setRoleCode(roleCode);
                        roleInfo.setRoleId(null); // ID 未知
                        roleInfo.setRoleName(null); // Name 未知
                        return roleInfo;
                    })
                    .collect(Collectors.toList());
            user.setRoles(roles);
            log.debug("参数解析器: 解析得到角色列表: {}", roles.stream().map(UserConstant.RoleInfo::getRoleCode).collect(Collectors.joining(",")));
        } else {
            log.debug("参数解析器: 请求头 '{}' 为空或不存在，用户角色列表为空。", RequestHeaders.HEADER_X_USER_ROLES);
            user.setRoles(Collections.emptyList()); // 如果没有角色头，设置为空列表
        }

        user.setIssuedAt(null);
        user.setExpiration(null);
        user.setClientFingerprint(null); // 这个指纹是 JWT 里的，网关校验用，通常不透传

        log.info("参数解析器: 成功从请求头解析 UserConstant: userId={},userCode={}, username={}, email={}, status={}, roles={}",
                user.getUserId(), user.getUserCode(),user.getUsername(), user.getEmail(), user.getStatus(),
                user.getRoles().stream().map(UserConstant.RoleInfo::getRoleCode).collect(Collectors.joining(",")));

        // 5. 返回构建好的 UserConstant 对象
        return user;
    }
}
