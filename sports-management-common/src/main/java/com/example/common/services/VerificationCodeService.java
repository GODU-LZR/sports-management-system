package com.example.common.services;

public interface VerificationCodeService {
    boolean sendCode(String email);
    boolean verifyCode(String email, String submittedCode);
}