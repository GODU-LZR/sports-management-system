package com.example.middleware.notification.service;



public interface ResendEmailService {
    String sendEmail(String to, String subject, String htmlBody);
}
