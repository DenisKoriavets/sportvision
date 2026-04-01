package com.github.deniskoriavets.sportvision.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationRequest(
    @NotBlank(message = "Значення email не може бути порожнім")
    @Email(message = "Некоректний формат email")
    String email
) {}