package com.github.deniskoriavets.sportvision.dto.request;

import com.github.deniskoriavets.sportvision.validation.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    String email,
    @NotBlank(message = "Password must not be blank")
    @Password
    String password,
    @NotBlank(message = "First name must not be blank")
    String firstName,
    @NotBlank(message = "Last name must not be blank")
    String lastName
) {
}