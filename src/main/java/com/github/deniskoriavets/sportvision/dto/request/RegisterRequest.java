package com.github.deniskoriavets.sportvision.dto.request;

import com.github.deniskoriavets.sportvision.validation.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank(message = "Значення email не може бути порожнім")
    @Email(message = "Некоректний формат email")
    String email,
    @NotBlank(message = "Значення пароля не може бути порожнім")
    @Password
    String password,
    @NotBlank(message = "Ім'я не може бути порожнім")
    String firstName,
    @NotBlank(message = "Прізвище не може бути порожнім")
    String lastName
) {
}