package com.example.middleware.utils;

import java.security.SecureRandom;
import java.util.Random;

public class VerificationCodeUtil {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int DEFAULT_LENGTH = 6;
    private static final Random RANDOM = new SecureRandom(); // 使用 SecureRandom 更安全

    /**
     * 生成指定长度的随机字母数字验证码
     * @param length 验证码长度
     * @return 生成的验证码
     */
    public static String generateCode(int length) {
        if (length <= 0) {
            length = DEFAULT_LENGTH; // 长度无效时使用默认长度
        }
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    /**
     * 生成默认长度 (6位) 的随机字母数字验证码
     * @return 生成的验证码
     */
    public static String generateDefaultCode() {
        return generateCode(DEFAULT_LENGTH);
    }

    // 私有构造函数，防止实例化
    private VerificationCodeUtil() {}
}