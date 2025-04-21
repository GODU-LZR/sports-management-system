package com.example.middleware.notification.listener;

import com.alibaba.fastjson2.JSON;
import com.example.middleware.notification.service.impl.ResendEmailServiceImpl;
import com.example.middleware.notification.dto.EmailCodeMessage;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class EmailQueueListener {

    @Autowired
    private ResendEmailServiceImpl resendEmailServiceImpl; // 注入你配置好的 Resend 邮件服务

    // 监听 application.properties 中配置的队列
    @RabbitListener(queues = "${rabbitmq.queue.email}")
    public void handleEmailCodeMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageBody = new String(message.getBody());
        log.info("接收到邮件发送任务消息，Delivery Tag: {}, Body: {}", deliveryTag, messageBody);

        try {
            // 将 JSON 字符串反序列化为 EmailCodeMessage 对象
            EmailCodeMessage emailCodeMessage = JSON.parseObject(messageBody, EmailCodeMessage.class);

            if (emailCodeMessage != null && emailCodeMessage.getEmail() != null && emailCodeMessage.getCode() != null) {
                // 构造邮件内容
                String subject = "您的验证码";
                String htmlBody = String.format("<h1>验证码</h1><p>您好，您的验证码是：<b>%s</b></p><p>此验证码将在 %d 秒后失效。</p>",
                        emailCodeMessage.getCode(), 120); // 过期时间可以从配置读取或固定

                // 调用邮件服务发送邮件
                // 注意：这里的 resendEmailService.sendEmail 返回的是 String (Email ID) 或 null
                // 我们需要判断是否发送成功（不为 null 或不抛异常）
                String emailId = resendEmailServiceImpl.sendEmail(emailCodeMessage.getEmail(), subject, htmlBody);

                if (emailId != null) {
                    log.info("验证码邮件已成功发送至 {}，邮件服务返回 ID: {}", emailCodeMessage.getEmail(), emailId);
                    // 确认消息已被成功处理
                    channel.basicAck(deliveryTag, false);
                    log.debug("消息已成功 ACK，Delivery Tag: {}", deliveryTag);
                } else {
                    log.error("调用邮件服务发送验证码失败，邮箱: {}", emailCodeMessage.getEmail());
                    // 发送失败，拒绝消息，让其重试或进入死信队列
                    // false 表示只拒绝当前消息，true 表示拒绝小于等于此 deliveryTag 的所有未确认消息
                    // true 表示消息重新入队，false 表示不重新入队（可能进入死信队列或被丢弃，取决于队列配置）
                    channel.basicNack(deliveryTag, false, false); 
                    log.warn("消息已 NACK (不重新入队)，Delivery Tag: {}", deliveryTag);
                }
            } else {
                log.error("接收到的消息格式不正确或缺少必要字段: {}", messageBody);
                // 消息格式错误，直接拒绝并不重新入队
                channel.basicNack(deliveryTag, false, false);
                 log.warn("格式错误的消息已 NACK (不重新入队)，Delivery Tag: {}", deliveryTag);
            }
        } catch (Exception e) {
            log.error("处理邮件发送任务时发生异常，Delivery Tag: {}", deliveryTag, e);
            // 发生未知异常，拒绝消息，不重新入队，防止无限循环处理错误消息
            channel.basicNack(deliveryTag, false, false);
            log.warn("发生异常的消息已 NACK (不重新入队)，Delivery Tag: {}", deliveryTag);
        }
    }
}