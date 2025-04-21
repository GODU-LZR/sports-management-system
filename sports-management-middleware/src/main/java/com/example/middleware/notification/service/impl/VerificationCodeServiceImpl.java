package com.example.middleware.notification.service.impl;

import com.example.common.services.VerificationCodeService;
import com.example.common.utils.RedisUtil;
import com.example.middleware.notification.dto.EmailCodeMessage;
// 确保 VerificationCodeService 接口的 import 路径正确
import com.example.middleware.rabbitmq.RabbitMQUtil;
import com.example.middleware.utils.VerificationCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.concurrent.TimeUnit;

@Slf4j
@DubboService(version = "1.0.0", interfaceClass = com.example.common.services.VerificationCodeService.class)// Dubbo 的 @Service 注解，用于暴露服务
@Service // Spring 的 @Service 也要保留，确保 Spring 能管理它
// 注意：如果你的 Dubbo 版本和 Spring Boot 集成方式允许，可以只用 Dubbo 的 @Service
// 但通常两者都保留是安全的，或者只用 Dubbo @Service 并确保 Dubbo 的扫描能找到它
public class VerificationCodeServiceImpl implements VerificationCodeService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RabbitMQUtil rabbitMQUtil;

    @Value("${verification.code.redis.prefix:verification_code:}")
    private String redisKeyPrefix;

    @Value("${verification.code.expire.seconds:120}")
    private long expireSeconds;

    @Value("${rabbitmq.queue.email}")
    private String emailQueueName;

    // sendCode 方法 (保持不变)
    @Override // 显式添加 @Override 是个好习惯
    public boolean sendCode(String email) {
        if (email == null || email.isEmpty()) {
            log.warn("发送验证码请求失败：邮箱地址为空");
            return false;
        }
        String code = VerificationCodeUtil.generateDefaultCode();
        String redisKey = redisKeyPrefix + email;
        boolean redisSet = redisUtil.set(redisKey, code, expireSeconds, TimeUnit.SECONDS);
        if (!redisSet) {
            log.error("将验证码存入 Redis 失败，邮箱: {}", email);
            return false;
        }
        log.info("验证码已生成并存入 Redis，邮箱: {}, Key: {}", email, redisKey);
        try {
            EmailCodeMessage message = new EmailCodeMessage(email, code);
            rabbitMQUtil.sendObject(message, emailQueueName);
            log.info("发送验证码任务已投递到 RabbitMQ 队列 '{}'，邮箱: {}", emailQueueName, email);
            return true;
        } catch (Exception e) {
            log.error("发送验证码任务到 RabbitMQ 失败，邮箱: {}", email, e);
            redisUtil.delete(redisKey);
            log.warn("由于 RabbitMQ 发送失败，已回滚删除 Redis 中的验证码 Key: {}", redisKey);
            return false;
        }
    }

    // verifyCode 方法 (保持不变)
    @Override // 显式添加 @Override 是个好习惯
    public boolean verifyCode(String email, String submittedCode) {
        if (email == null || email.isEmpty() || submittedCode == null || submittedCode.isEmpty()) {
            log.warn("验证码校验失败：邮箱或提交的验证码为空");
            return false;
        }
        String redisKey = redisKeyPrefix + email;
        Object storedCodeObj = redisUtil.get(redisKey);
        if (storedCodeObj == null) {
            log.warn("验证码校验失败：未在 Redis 中找到 Key '{}' (可能已过期或从未发送)", redisKey);
            return false;
        }
        String storedCode = String.valueOf(storedCodeObj);
        boolean isValid = storedCode.equalsIgnoreCase(submittedCode);
        if (isValid) {
            log.info("邮箱 {} 验证码校验成功", email);
            boolean deleted = redisUtil.delete(redisKey);
            if (!deleted) {
                log.warn("验证成功后删除 Redis Key '{}' 失败，可能已被其他操作删除", redisKey);
            }
            return true;
        } else {
            log.warn("邮箱 {} 验证码校验失败：提交的验证码 '{}' 与存储的验证码 '{}' 不匹配", email, submittedCode, storedCode);
            return false;
        }
    }
}