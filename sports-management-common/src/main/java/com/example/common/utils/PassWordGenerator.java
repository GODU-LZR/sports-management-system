package com.example.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Base64; // 导入 Base64 工具类
import java.nio.charset.StandardCharsets; // 导入字符集

public class PassWordGenerator {
    public static void main(String[] args) {
        // --- 生成 BCrypt 加密密码 ---
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "gatewaypass";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("BCrypt 加密后的密码: " + encodedPassword);

        // --- 生成 Basic Auth 的 Base64 编码 ---
        String username = "gatewayuser";
        // 注意：这里使用的是原始明文密码 rawPassword
        String basicAuthValue = generateBasicAuthHeaderValue(username, rawPassword);
        System.out.println("Basic Auth Header 值 (Base64编码): " + basicAuthValue);

        // --- 直接对固定字符串进行编码 ---
        String credentials = "gatewayuser:gatewaypass";
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        System.out.println("直接编码 'gatewayuser:gatewaypass': " + encodedCredentials);
    }

    /**
     * 生成用于 HTTP Basic Authentication Header 的 Base64 编码值。
     *
     * @param username 用户名 (明文)
     * @param password 密码 (明文)
     * @return "username:password" 字符串经过 Base64 编码后的结果
     */
    public static String generateBasicAuthHeaderValue(String username, String password) {
        String credentials = username + ":" + password;
        // 使用 UTF-8 编码获取字节数组，然后进行 Base64 编码
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
