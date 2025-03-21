package com.example.user.service;

import com.example.common.model.Result;
import com.example.user.dto.LoginRequest;
import com.example.user.dto.LoginResponse;
import com.example.user.dto.RegistrationRequest;

public interface AuthService {
    Result<LoginResponse> register(RegistrationRequest registrationRequest);
    Result<LoginResponse> login(LoginRequest loginRequest);
    Result<Void> logout(String token);
}
