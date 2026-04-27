package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.config.JwtProperties;
import com.github.deniskoriavets.sportvision.dto.response.AuthResponse;
import com.github.deniskoriavets.sportvision.dto.request.LoginRequest;
import com.github.deniskoriavets.sportvision.dto.request.RegisterRequest;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.VerificationToken;
import com.github.deniskoriavets.sportvision.exception.EmailAlreadyTakenException;
import com.github.deniskoriavets.sportvision.repository.ParentRepository;
import com.github.deniskoriavets.sportvision.repository.RefreshTokenRepository;
import com.github.deniskoriavets.sportvision.repository.VerificationTokenRepository;
import com.github.deniskoriavets.sportvision.security.JwtService;
import com.github.deniskoriavets.sportvision.notification.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private ParentRepository parentRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private JwtProperties jwtProperties;
    @Mock private EmailService emailService;
    @Mock private VerificationTokenRepository verificationTokenRepository;

    @InjectMocks private AuthServiceImpl authService;

    @Test
    @DisplayName("Registers new parent successfully")
    void register_Success() {
        RegisterRequest request = new RegisterRequest("test@test.com", "Password123!", "Іван", "Іванов");

        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(parentRepository.save(any())).thenReturn(new Parent());
        when(verificationTokenRepository.save(any())).thenReturn(new VerificationToken());

        authService.register(request);

        verify(parentRepository).save(any());
        verify(emailService).sendVerificationEmail(any(), any());
    }

    @Test
    @DisplayName("Throws exception when email is already taken")
    void register_ThrowsException_WhenEmailTaken() {
        RegisterRequest request = new RegisterRequest("test@test.com", "Password123!", "Іван", "Іванов");
        when(parentRepository.save(any())).thenThrow(DataIntegrityViolationException.class);

        assertThrows(EmailAlreadyTakenException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("Login succeeds with valid credentials")
    void login_Success() {
        LoginRequest request = new LoginRequest("test@test.com", "Password123!");
        Parent parent = Parent.builder()
            .email("test@test.com")
            .passwordHash("encodedPassword")
            .isEmailVerified(true)
            .isActive(true)
            .build();

        when(parentRepository.findByEmail(request.email())).thenReturn(Optional.of(parent));
        when(passwordEncoder.matches(request.password(), parent.getPasswordHash())).thenReturn(true);
        when(jwtService.generateAccessToken(parent)).thenReturn("access");
        when(jwtService.generateRefreshToken(parent)).thenReturn("refresh");

        when(jwtProperties.getRefreshToken()).thenReturn(new JwtProperties.RefreshToken());

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        verify(refreshTokenRepository).save(any());
    }
}