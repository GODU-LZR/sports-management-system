package com.example.middleware.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ResendEmailService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);

    @Autowired(required = false)
    private RestTemplate restTemplate; // 确保 RestTemplate Bean 已配置, Spring Boot 会自动配置一个

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.sender.email}")
    private String senderEmail;

    @Value("${resend.api.url}")
    private String resendApiUrl;

    /**
     * 发送邮件
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param htmlBody 邮件内容 (HTML格式)
     * @return Resend 返回的邮件 ID 或 null (如果失败)
     */
    public String sendEmail(String to, String subject, String htmlBody) {
        log.info("Attempting to send email via Resend from {} to {}", senderEmail, to);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey); // 设置 Authorization Header

        // 构建 Resend API 请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("from", senderEmail);
        requestBody.put("to", to);
        requestBody.put("subject", subject);
        requestBody.put("html", htmlBody); // 或者使用 "text" 发送纯文本

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // 发送 POST 请求
            ResponseEntity<Map> response = restTemplate.postForEntity(resendApiUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().containsKey("id")) {
                String emailId = (String) response.getBody().get("id");
                log.info("Email sent successfully via Resend. Email ID: {}", emailId);
                return emailId;
            } else {
                log.error("Failed to send email via Resend. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                return null;
            }
        } catch (HttpClientErrorException e) {
            log.error("Error sending email via Resend: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error sending email via Resend", e);
            return null;
        }
    }

    // 可选: 配置 RestTemplate Bean (如果需要自定义)
    // 通常 Spring Boot 会自动配置一个，除非你需要特殊设置
    // @Bean 
    // public RestTemplate restTemplate() {
    //     return new RestTemplate();
    // }
}