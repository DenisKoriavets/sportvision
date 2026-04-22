package com.github.deniskoriavets.sportvision.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationRequest(
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    String email
) {}