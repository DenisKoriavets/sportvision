package com.github.deniskoriavets.sportvision.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Значення email не може бути порожнім")
    @Email(message = "Некоректний формат email")
    String email,
    @NotBlank(message = "Значення пароля не може бути порожнім")
    String password
) {}