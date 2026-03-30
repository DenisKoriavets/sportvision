package com.github.deniskoriavets.sportvision.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken
) {}