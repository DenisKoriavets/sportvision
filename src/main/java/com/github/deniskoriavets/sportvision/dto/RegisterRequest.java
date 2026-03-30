package com.github.deniskoriavets.sportvision.dto;

import com.github.deniskoriavets.sportvision.security.validator.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @Email(message = "Некоректний формат email")
    String email,
    @Password
    String password,
    @NotBlank(message = "Ім'я не може бути порожнім")
    String firstName,
    String lastName
) {}