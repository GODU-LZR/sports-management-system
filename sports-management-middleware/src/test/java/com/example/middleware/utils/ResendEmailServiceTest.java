package com.example.middleware.utils;

import com.example.middleware.notification.ResendEmailService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// 加载 Spring Boot 应用上下文进行集成测试
@SpringBootTest 
class ResendEmailServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailServiceTest.class);

    @Autowired(required = false)
    private ResendEmailService emailService; // 注入你的邮件服务

    // !!! 重要提示 !!!
    // 这是一个集成测试，它会真实地调用 Resend API 发送邮件并消耗你的额度。
    // 在 CI/CD 或频繁测试中，通常建议使用 Mockito 等工具模拟(Mock)外部服务调用，
    // 而不是每次都真实发送。
    // 
    // 如果你只想运行一次来验证配置和基本功能，可以去掉 @Disabled 注解。
    // 如果想避免实际发送，请保持 @Disabled 并学习如何 Mock RestTemplate 的调用。
    @Test
    // @Disabled("Remove this annotation to run the actual API call test (will send email)")
    void sendActualEmailTest() {
        // --- 测试参数 ---
        String recipientEmail = "2980560533@qq.com"; // 你的接收邮箱
        String subject = "Spring Boot Resend Test";
        String htmlContent = "<h1>Hello from Spring Boot!</h1><p>This is a test email sent via Resend.</p>";

        log.info("Starting actual email send test to {}", recipientEmail);

        // 调用发送邮件方法，并断言它没有抛出异常且返回了邮件 ID (非 null)
        // 使用 assertDoesNotThrow 来确保 API 调用过程中没有意外中断
        Assertions.assertDoesNotThrow(() -> {
            String emailId = emailService.sendEmail(recipientEmail, subject, htmlContent);
            Assertions.assertNotNull(emailId, "Email ID should not be null on successful send.");
            log.info("Test email potentially sent successfully. Check recipient inbox. Email ID: {}", emailId);
        }, "Sending email should not throw an exception.");

        // 注意：这个测试只验证了 API 调用成功（Resend 接受了请求），
        // 邮件是否真正送达并进入收件箱还需要检查你的 QQ 邮箱。
    }

    @Test
    void basicContextLoads() {
        // 一个简单的测试，确保 Spring 上下文能成功加载
        Assertions.assertNotNull(emailService, "EmailService should be injected");
    }
}