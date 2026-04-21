package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.response.AuthResponse;
import com.github.deniskoriavets.sportvision.dto.request.LoginRequest;
import com.github.deniskoriavets.sportvision.dto.request.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest registerRequest);

    AuthResponse login(LoginRequest loginRequest);

    AuthResponse refresh(String refreshTokenRequest);

    void logout(String refreshTokenRequest);

    void verifyEmail(String token);

    void resendVerification(String email);
}
