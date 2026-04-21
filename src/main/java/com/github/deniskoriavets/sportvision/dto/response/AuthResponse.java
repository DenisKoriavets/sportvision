package com.github.deniskoriavets.sportvision.dto.response;

public record AuthResponse(
    String accessToken,
    String refreshToken
) {}