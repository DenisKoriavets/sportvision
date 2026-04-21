package com.github.deniskoriavets.sportvision.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @NotBlank(message = "Refresh token не може бути порожнім")
    String refreshToken
) {}