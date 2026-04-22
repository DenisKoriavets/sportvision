package com.github.deniskoriavets.sportvision.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @NotBlank(message = "Refresh token must not be blank")
    String refreshToken
) {}