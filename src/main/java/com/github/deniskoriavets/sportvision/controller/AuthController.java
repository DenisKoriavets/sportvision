package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.AuthResponse;
import com.github.deniskoriavets.sportvision.dto.LoginRequest;
import com.github.deniskoriavets.sportvision.dto.RegisterRequest;
import com.github.deniskoriavets.sportvision.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Ендпоінти для реєстрації, логіну та керування сесіями")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    @Operation(summary = "Автентифікація користувача та отримання токенів")
    public ResponseEntity<AuthResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/verify")
    @Operation(summary = "Підтвердження пошти")
    public ResponseEntity<Void> verify(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Оновлення Access Token за допомогою Refresh Token")
    public ResponseEntity<AuthResponse> refresh(@RequestBody String refreshToken) {
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Повторна відправка листа")
    public ResponseEntity<Void> resendVerification(@RequestParam String email) {
        authService.resendVerification(email);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/logout")
    @Operation(summary = "Вихід із системи (анулювання токена)")
    public ResponseEntity<Void> logout(@RequestBody String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent().build();
    }
}
