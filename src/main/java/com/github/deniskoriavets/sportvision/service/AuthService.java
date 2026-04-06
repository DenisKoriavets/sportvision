package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.AuthResponse;
import com.github.deniskoriavets.sportvision.dto.LoginRequest;
import com.github.deniskoriavets.sportvision.dto.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest registerRequest);

    AuthResponse login(LoginRequest loginRequest);

    AuthResponse refresh(String refreshTokenRequest);

    void logout(String refreshTokenRequest);

    void verifyEmail(String token);

    void resendVerification(String email);
}
