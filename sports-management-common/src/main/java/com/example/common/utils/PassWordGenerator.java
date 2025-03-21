package com.example.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PassWordGenerator {
    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "gatewaypass";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println(encodedPassword);  // 输出或存储这个加密后的密码
    }

}
