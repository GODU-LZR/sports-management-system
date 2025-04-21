package com.example.middleware.utils; // 确认包名是否正确

import com.example.common.utils.RedisUtil; // 引入 RedisUtil 用于辅助测试
import com.example.middleware.notification.dto.SendCodeRequest;
import com.example.middleware.notification.dto.VerifyCodeRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*; // 引入 HttpHeaders, HttpEntity, HttpMethod, MediaType
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VerificationControllerTest {

    private static final Logger log = LoggerFactory.getLogger(VerificationControllerTest.class);

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Value("${verification.code.redis.prefix:verification_code:}")
    private String redisKeyPrefix;

    private final String testEmail = "2980560533@qq.com";

    // --- 定义请求头常量 ---
    private final String AUTH_HEADER_VALUE = "Basic Z2F0ZXdheXVzZXI6Z2F0ZXdheXBhc3M="; // 使用你提供的值
    private final String ROLES_HEADER_VALUE = "USER";

    @BeforeEach
    void setUp() {
        // 清理 Redis Key
         String redisKey = redisKeyPrefix + testEmail;
         redisUtil.delete(redisKey);
         log.info("Cleaned up Redis key '{}' before test.", redisKey);
    }

    /**
     * 辅助方法：创建带有认证和角色请求头的 HttpHeaders
     * @return HttpHeaders 对象
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", AUTH_HEADER_VALUE);
        headers.set("X-User-Roles", ROLES_HEADER_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON); // 设置请求体类型为 JSON
        return headers;
    }

    @Test
    @DisplayName("测试发送验证码接口 - 成功流程 (带请求头)")
    void testSendVerificationCode_Success() {
        log.info("Starting test: Send Verification Code - Success (with Headers)");
        SendCodeRequest requestDto = new SendCodeRequest();
        requestDto.setEmail(testEmail);

        // 创建请求头
        HttpHeaders headers = createAuthHeaders();

        // 创建包含请求体和请求头的 HttpEntity
        HttpEntity<SendCodeRequest> requestEntity = new HttpEntity<>(requestDto, headers);

        // 使用 exchange 方法发送 POST 请求
        ResponseEntity<String> response = testRestTemplate.exchange(
                "/verification/sendCode",
                HttpMethod.POST, // 指定请求方法
                requestEntity,   // 传入 HttpEntity
                String.class     // 期望的响应体类型
        );

        // 断言结果 (与之前相同)
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(response.getBody() != null && response.getBody().contains("验证码发送任务已触发"));
        log.info("Send code API call successful, response: {}", response.getBody());
        Assertions.assertTrue(redisUtil.hasKey(redisKeyPrefix + testEmail), "Redis should contain the key after sending code");
        log.info("Verified that Redis key exists for email: {}", testEmail);
    }

    @Test
    @DisplayName("测试校验验证码接口 - 成功 (带请求头)")
    void testVerifyVerificationCode_Success() {
        log.info("Starting test: Verify Verification Code - Success (with Headers)");
        String correctCode = "ABC123";
        String redisKey = redisKeyPrefix + testEmail;

        // 1. Setup: 在 Redis 中设置验证码
        boolean setResult = redisUtil.set(redisKey, correctCode, 120, TimeUnit.SECONDS);
        Assertions.assertTrue(setResult, "Failed to set verification code in Redis for test setup");
        log.info("Manually set verification code '{}' for email '{}' in Redis", correctCode, testEmail);

        // 2. 构造校验请求 DTO
        VerifyCodeRequest requestDto = new VerifyCodeRequest();
        requestDto.setEmail(testEmail);
        requestDto.setCode(correctCode);

        // 3. 创建请求头和 HttpEntity
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<VerifyCodeRequest> requestEntity = new HttpEntity<>(requestDto, headers);

        // 4. 使用 exchange 方法发送 POST 请求
        ResponseEntity<Boolean> response = testRestTemplate.exchange(
                "/verification/verifyCode",
                HttpMethod.POST,
                requestEntity,
                Boolean.class // 期望响应体是 Boolean 类型
        );

        // 5. 断言结果
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(Boolean.TRUE, response.getBody());
        Assertions.assertFalse(redisUtil.hasKey(redisKey), "Redis key should be deleted after successful verification");
        log.info("Verification successful and Redis key '{}' was correctly deleted.", redisKey);
    }

    @Test
    @DisplayName("测试校验验证码接口 - 失败 (验证码错误, 带请求头)")
    void testVerifyVerificationCode_WrongCode() {
        log.info("Starting test: Verify Verification Code - Wrong Code (with Headers)");
        String correctCode = "XYZ789";
        String wrongCode = "WRONG1";
        String redisKey = redisKeyPrefix + testEmail;

        // 1. Setup: 在 Redis 中设置正确的验证码
        redisUtil.set(redisKey, correctCode, 120, TimeUnit.SECONDS);
        log.info("Manually set verification code '{}' for email '{}' in Redis", correctCode, testEmail);

        // 2. 构造校验请求 DTO
        VerifyCodeRequest requestDto = new VerifyCodeRequest();
        requestDto.setEmail(testEmail);
        requestDto.setCode(wrongCode); // 使用错误的验证码

        // 3. 创建请求头和 HttpEntity
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<VerifyCodeRequest> requestEntity = new HttpEntity<>(requestDto, headers);

        // 4. 发起 POST 请求
        ResponseEntity<Boolean> response = testRestTemplate.exchange(
                "/verification/verifyCode",
                HttpMethod.POST,
                requestEntity,
                Boolean.class
        );

        // 5. 断言结果
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(Boolean.FALSE, response.getBody());
        Assertions.assertTrue(redisUtil.hasKey(redisKey), "Redis key should still exist after wrong code verification");
        Assertions.assertEquals(correctCode, redisUtil.get(redisKey), "Redis value should remain unchanged");
        log.info("Verification failed as expected. Redis key '{}' still exists with the original code.", redisKey);
    }

    @Test
    @DisplayName("测试校验验证码接口 - 失败 (验证码不存在或已过期, 带请求头)")
    void testVerifyVerificationCode_CodeNotFoundOrExpired() {
        log.info("Starting test: Verify Verification Code - Code Not Found / Expired (with Headers)");
        String nonExistentCode = "ANYCDE";
        String redisKey = redisKeyPrefix + testEmail;

        // 1. Setup: 确保 Redis 中没有这个 key
        Assertions.assertFalse(redisUtil.hasKey(redisKey), "Redis key should not exist before the test");
        log.info("Ensured Redis key '{}' does not exist for setup.", redisKey);

        // 2. 构造校验请求 DTO
        VerifyCodeRequest requestDto = new VerifyCodeRequest();
        requestDto.setEmail(testEmail);
        requestDto.setCode(nonExistentCode);

        // 3. 创建请求头和 HttpEntity
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<VerifyCodeRequest> requestEntity = new HttpEntity<>(requestDto, headers);

        // 4. 发起 POST 请求
        ResponseEntity<Boolean> response = testRestTemplate.exchange(
                "/verification/verifyCode",
                HttpMethod.POST,
                requestEntity,
                Boolean.class
        );

        // 5. 断言结果
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(Boolean.FALSE, response.getBody());
        log.info("Verification failed as expected because code was not found in Redis.");
    }


    @Test
    @DisplayName("测试发送验证码接口 - 邮箱格式无效 (带请求头)")
    void testSendVerificationCode_InvalidEmail() {
        log.info("Starting test: Send Verification Code - Invalid Email (with Headers)");
        SendCodeRequest requestDto = new SendCodeRequest();
        requestDto.setEmail("invalid-email-format"); // 无效邮箱

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<SendCodeRequest> requestEntity = new HttpEntity<>(requestDto, headers);

        ResponseEntity<String> response = testRestTemplate.exchange(
                "/verification/sendCode",
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // 由于 @Valid 生效，应该返回 400 Bad Request
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        log.info("Received expected BAD_REQUEST for invalid email format.");
    }

    @Test
    void basicContextLoads() {
        Assertions.assertNotNull(testRestTemplate, "TestRestTemplate should be injected");
        Assertions.assertNotNull(redisUtil, "RedisUtil should be injected");
    }
}