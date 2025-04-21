package com.example.middleware.utils; // 或 com.example.middleware.service

import com.example.common.services.VerificationCodeService;
import com.example.common.utils.RedisUtil; // 确认导入

import com.example.middleware.notification.dto.EmailCodeMessage;
import com.example.middleware.rabbitmq.RabbitMQUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito; // 引入 Mockito
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean; // 引入 @MockBean

import java.util.concurrent.TimeUnit;

// 加载 Spring Boot 应用上下文进行 Service 层集成测试 (不需要 Web 环境)
@SpringBootTest
// 建议重命名此类为 VerificationCodeServiceTest
class VerificationCodeServiceTest {

    private static final Logger log = LoggerFactory.getLogger(VerificationCodeServiceTest.class); // 更新 Logger

    @Autowired
    private VerificationCodeService verificationCodeService; // 注入要测试的服务

    @Autowired
    private RedisUtil redisUtil; // 仍然注入 RedisUtil 来验证 Redis 操作

    @MockBean // 使用 @MockBean 来模拟 RabbitMQUtil 的行为
    private RabbitMQUtil rabbitMQUtil;

    @Value("${verification.code.redis.prefix:verification_code:}")
    private String redisKeyPrefix;

    @Value("${verification.code.expire.seconds:120}")
    private long expireSeconds;

    @Value("${rabbitmq.queue.email:email_verification_queue}")
    private String emailQueueName;

    private final String testEmail = "2980560533@qq.com";
    private String testRedisKey;

    @BeforeEach
    void setUp() {
        testRedisKey = redisKeyPrefix + testEmail;
        // 清理 Redis 中的测试 Key，确保测试隔离性
        redisUtil.delete(testRedisKey);
        log.info("Cleaned up Redis key '{}' before test.", testRedisKey);
        // 重置 Mockito 的交互记录 (如果需要跨测试方法验证，则不需要 reset)
        // Mockito.reset(rabbitMQUtil);
    }

    @Test
    @DisplayName("测试 sendCode 方法 - 成功流程")
    void testSendCode_Success() {
        log.info("Starting test: VerificationCodeService.sendCode() - Success");

        // 执行 sendCode 方法
        boolean result = verificationCodeService.sendCode(testEmail);

        // 1. 断言方法返回 true
        Assertions.assertTrue(result, "sendCode should return true on success");

        // 2. 验证 Redis 中是否正确存储了验证码
        Assertions.assertTrue(redisUtil.hasKey(testRedisKey), "Redis key should exist after sendCode");
        Object storedCodeObj = redisUtil.get(testRedisKey);
        Assertions.assertNotNull(storedCodeObj, "Stored code in Redis should not be null");
        String storedCode = String.valueOf(storedCodeObj);
        Assertions.assertEquals(6, storedCode.length(), "Stored code should have length 6");
        log.info("Verified code stored in Redis for key '{}'", testRedisKey);
        // 验证过期时间 (大约)
        long ttl = redisUtil.getExpire(testRedisKey);
        Assertions.assertTrue(ttl > 0 && ttl <= expireSeconds, "Redis key TTL should be close to " + expireSeconds);
        log.info("Verified Redis key TTL is approximately {}", ttl);


        // 3. 验证 RabbitMQUtil 的 sendObject 方法是否被调用了一次
        // 使用 ArgumentCaptor 捕获传递给 mock 对象方法的参数
        ArgumentCaptor<EmailCodeMessage> messageCaptor = ArgumentCaptor.forClass(EmailCodeMessage.class);
        ArgumentCaptor<String> queueNameCaptor = ArgumentCaptor.forClass(String.class);

        // 验证 rabbitMQUtil.sendObject 方法被调用了一次，并捕获参数
        Mockito.verify(rabbitMQUtil, Mockito.times(1))
                .sendObject(messageCaptor.capture(), queueNameCaptor.capture());

        // 验证捕获到的参数是否符合预期
        EmailCodeMessage capturedMessage = messageCaptor.getValue();
        String capturedQueueName = queueNameCaptor.getValue();

        Assertions.assertEquals(testEmail, capturedMessage.getEmail(), "Email in RabbitMQ message should match");
        Assertions.assertEquals(storedCode, capturedMessage.getCode(), "Code in RabbitMQ message should match stored code");
        Assertions.assertEquals(emailQueueName, capturedQueueName, "Queue name for RabbitMQ should match configuration");

        log.info("Verified RabbitMQUtil.sendObject was called correctly.");
    }

    @Test
    @DisplayName("测试 verifyCode 方法 - 成功")
    void testVerifyCode_Success() {
        log.info("Starting test: VerificationCodeService.verifyCode() - Success");
        String correctCode = "PASS12";

        // 1. Setup: 在 Redis 中设置验证码
        redisUtil.set(testRedisKey, correctCode, expireSeconds, TimeUnit.SECONDS);
        Assertions.assertTrue(redisUtil.hasKey(testRedisKey), "Setup failed: Redis key not set");
        log.info("Setup: Manually set code '{}' in Redis for key '{}'", correctCode, testRedisKey);

        // 2. 执行 verifyCode 方法
        boolean result = verificationCodeService.verifyCode(testEmail, correctCode);

        // 3. 断言方法返回 true
        Assertions.assertTrue(result, "verifyCode should return true for correct code");

        // 4. 验证 Redis 中的 key 是否已被删除
        Assertions.assertFalse(redisUtil.hasKey(testRedisKey), "Redis key should be deleted after successful verification");
        log.info("Verified Redis key '{}' was deleted after successful verification.", testRedisKey);

        // 5. 验证 RabbitMQUtil 没有被调用 (因为校验操作不涉及MQ)
        Mockito.verify(rabbitMQUtil, Mockito.never()).sendObject(Mockito.any(), Mockito.anyString());
        log.info("Verified RabbitMQUtil was not called during verification.");

    }

    @Test
    @DisplayName("测试 verifyCode 方法 - 失败 (验证码错误)")
    void testVerifyCode_WrongCode() {
        log.info("Starting test: VerificationCodeService.verifyCode() - Wrong Code");
        String correctCode = "REALCD";
        String wrongCode = "FAKECD";

        // 1. Setup: 在 Redis 中设置正确的验证码
        redisUtil.set(testRedisKey, correctCode, expireSeconds, TimeUnit.SECONDS);
        Assertions.assertTrue(redisUtil.hasKey(testRedisKey), "Setup failed: Redis key not set");
        log.info("Setup: Manually set code '{}' in Redis for key '{}'", correctCode, testRedisKey);

        // 2. 执行 verifyCode 方法 (使用错误的 code)
        boolean result = verificationCodeService.verifyCode(testEmail, wrongCode);

        // 3. 断言方法返回 false
        Assertions.assertFalse(result, "verifyCode should return false for wrong code");

        // 4. 验证 Redis 中的 key 仍然存在且值未改变
        Assertions.assertTrue(redisUtil.hasKey(testRedisKey), "Redis key should still exist after wrong code verification");
        Assertions.assertEquals(correctCode, redisUtil.get(testRedisKey), "Redis value should remain unchanged");
        log.info("Verified Redis key '{}' still exists with original value after wrong code verification.", testRedisKey);

        // 5. 验证 RabbitMQUtil 没有被调用
        Mockito.verify(rabbitMQUtil, Mockito.never()).sendObject(Mockito.any(), Mockito.anyString());
        log.info("Verified RabbitMQUtil was not called during verification.");
    }

    @Test
    @DisplayName("测试 verifyCode 方法 - 失败 (验证码不存在或已过期)")
    void testVerifyCode_NotFoundOrExpired() {
        log.info("Starting test: VerificationCodeService.verifyCode() - Not Found / Expired");
        String anyCode = "SOMECODE";

        // 1. Setup: 确保 Redis 中没有 Key
        Assertions.assertFalse(redisUtil.hasKey(testRedisKey), "Setup failed: Redis key should not exist");
        log.info("Setup: Ensured Redis key '{}' does not exist.", testRedisKey);

        // 2. 执行 verifyCode 方法
        boolean result = verificationCodeService.verifyCode(testEmail, anyCode);

        // 3. 断言方法返回 false
        Assertions.assertFalse(result, "verifyCode should return false if code not found in Redis");
        log.info("Verified verifyCode returned false when code not found in Redis.");

        // 4. 验证 RabbitMQUtil 没有被调用
        Mockito.verify(rabbitMQUtil, Mockito.never()).sendObject(Mockito.any(), Mockito.anyString());
        log.info("Verified RabbitMQUtil was not called during verification.");
    }

    @Test
    @DisplayName("测试 sendCode 方法 - 邮箱为空")
    void testSendCode_NullEmail() {
        log.info("Starting test: VerificationCodeService.sendCode() - Null Email");
        boolean result = verificationCodeService.sendCode(null);
        Assertions.assertFalse(result, "sendCode should return false for null email");

        // 验证 Redis 和 RabbitMQ 都未被调用
        Mockito.verify(rabbitMQUtil, Mockito.never()).sendObject(Mockito.any(), Mockito.anyString());
        Assertions.assertFalse(redisUtil.hasKey(redisKeyPrefix + "null"), "No Redis key should be created for null email"); // 检查潜在的错误 key
        log.info("Verified sendCode returns false and no external interactions for null email.");
    }

    // 可以保留 basicContextLoads 测试，确认服务和 RedisUtil 被注入
    @Test
    void basicContextLoads() {
        Assertions.assertNotNull(verificationCodeService, "VerificationCodeService should be injected");
        Assertions.assertNotNull(redisUtil, "RedisUtil should be injected");
        Assertions.assertNotNull(rabbitMQUtil, "RabbitMQUtil (MockBean) should be injected"); // 确认 Mock Bean 也被注入了
    }
}