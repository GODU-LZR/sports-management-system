package com.example.common.config;

import com.example.common.model.Result;
import com.example.common.model.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    // 固定系统账号：网关传递给下游的账号
    private static final String SYSTEM_USERNAME = "gatewayuser";
    // 这里的密码是 "gatewaypass" 使用 BCryptPasswordEncoder 加密后的值
    private static final String SYSTEM_PASSWORD = "$2a$10$F9kJs5I85nRpeOiw5edUteRm3btlDzA5Db7LnpmrWhCz0C1UK9QPC";

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 允许跨域预检请求及部分公开接口
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/api/**/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/**/v3/api-docs/**",
                        "/register",
                        "/login"
                ).permitAll()
                .anyRequest().authenticated()
                .and()
                .httpBasic()  // 启用 HTTP Basic 认证
                .authenticationEntryPoint(authenticationEntryPoint());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 使用自定义的 UserDetailsService 来构造认证信息
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            System.out.println("Downstream UserDetailsService 被调用");
            String roles = request.getHeader("X-User-Roles");
            System.out.println("Downstream 获取角色编码 X-User-Roles: " + roles);

            // 只处理固定的系统账号（gatewayuser），并要求 X-User-Roles 非空
            if (SYSTEM_USERNAME.equals(username) && StringUtils.hasText(roles)) {
                // 解析角色（以逗号分隔），并在前面加上 "ROLE_"
                List<String> authorityList = Arrays.stream(roles.split(","))
                        .map(String::trim)
                        .map(role -> "ROLE_" + role)
                        .collect(Collectors.toList());

                // 返回 UserDetails 对象，用户名为固定系统账号，密码为 BCrypt 加密后的 gatewaypass
                return User.withUsername(username)
                        .password(SYSTEM_PASSWORD)
                        .authorities(authorityList.toArray(new String[0]))
                        .build();
            }
            throw new UsernameNotFoundException("User not found: " + username);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 使用 BCryptPasswordEncoder
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            Result<?> result = Result.error(ResultCode.FORBIDDEN);
            response.setStatus(403);
            response.setContentType("application/json");
            response.getWriter().write(result.toString());
        };
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            log.error("Authentication failed: {}", authException.getMessage());
            log.info("Request URL: {}", request.getRequestURL());
            log.info("Request Headers: {}", Collections.list(request.getHeaderNames()));
            Result<?> result = Result.error(ResultCode.UNAUTHORIZED);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write(result.toString());
        };
    }
}
