package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.BaseIntegrationTest;
import com.github.deniskoriavets.sportvision.dto.request.LoginRequest;
import com.github.deniskoriavets.sportvision.dto.request.RefreshTokenRequest;
import com.github.deniskoriavets.sportvision.dto.request.RegisterRequest;
import com.github.deniskoriavets.sportvision.entity.*;
import com.github.deniskoriavets.sportvision.entity.enums.Role;
import com.github.deniskoriavets.sportvision.repository.*;
import com.github.deniskoriavets.sportvision.notification.EmailService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        truncateAll();
    }

    @Test
    @DisplayName("should register parent successfully")
    void shouldRegisterParentSuccessfully() throws Exception {
        var registrationEmail = "new-parent@ukma.edu.ua";

        var request =
            new RegisterRequest(registrationEmail, "StrongPassword123!", "Denis", "Koriavets");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        var savedParent = parentRepository.findByEmail(registrationEmail)
            .orElseThrow(() -> new AssertionError("Parent не був збережений у БД"));

        assertThat(savedParent.getFirstName()).isEqualTo("Denis");
        assertThat(savedParent.getLastName()).isEqualTo("Koriavets");

        verify(emailService).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("should login and return tokens when credentials are valid")
    void shouldLoginSuccessfully() throws Exception {
        var email = "denis@ukma.edu.ua";
        var password = "StrongPassword123!";

        Parent parent = Parent.builder()
            .firstName("Denis")
            .lastName("Koriavets")
            .email(email)
            .passwordHash(passwordEncoder.encode(password))
            .role(Role.PARENT)
            .isEmailVerified(true)
            .isActive(true)
            .build();
        parentRepository.save(parent);

        var loginRequest = new LoginRequest(email, password);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    @DisplayName("should verify email when token is valid")
    void shouldVerifyEmailSuccessfully() throws Exception {
        var email = "verify@ukma.edu.ua";
        Parent parent = parentRepository.save(Parent.builder()
            .email(email)
            .firstName("Test")
            .lastName("User")
            .passwordHash("hash")
            .role(Role.PARENT)
            .isEmailVerified(false)
            .build());

        String tokenValue = UUID.randomUUID().toString();
        tokenRepository.save(VerificationToken.builder()
            .token(tokenValue)
            .parent(parent)
            .expiryDate(LocalDateTime.now().plusHours(24))
            .build());

        mockMvc.perform(get("/api/v1/auth/verify")
                .param("token", tokenValue))
            .andExpect(status().isOk());

        var updatedParent = parentRepository.findByEmail(email).orElseThrow();
        assertThat(updatedParent.isEmailVerified()).isTrue();
    }

    @Test
    @DisplayName("should refresh token successfully")
    void shouldRefreshTokenSuccessfully() throws Exception {
        Parent parent = parentRepository.save(Parent.builder()
            .email("refresh@ukma.edu.ua")
            .firstName("Ref")
            .lastName("Resh")
            .passwordHash("hash")
            .role(Role.PARENT)
            .isEmailVerified(true)
            .isActive(true)
            .build());

        String oldRefreshTokenValue = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshToken.builder()
            .token(oldRefreshTokenValue)
            .parent(parent)
            .expiryDate(Instant.now().plusSeconds(3600))
            .revoked(false)
            .build());

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(new RefreshTokenRequest(oldRefreshTokenValue))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    @DisplayName("should logout and remove refresh token")
    void shouldLogoutSuccessfully() throws Exception {
        Parent parent = parentRepository.save(Parent.builder()
            .email("logout@ukma.edu.ua")
            .firstName("Logout")
            .lastName("Test")
            .passwordHash(passwordEncoder.encode("StrongPassword123!"))
            .role(Role.PARENT)
            .isEmailVerified(true)
            .isActive(true)
            .build());

        String refreshTokenValue = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshToken.builder()
            .token(refreshTokenValue)
            .parent(parent)
            .expiryDate(Instant.now().plusSeconds(3600))
            .revoked(false)
            .build());

        var loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new LoginRequest("logout@ukma.edu.ua", "StrongPassword123!"))))
            .andExpect(status().isOk())
            .andReturn();

        String accessToken = objectMapper
            .readTree(loginResponse.getResponse().getContentAsString())
            .get("accessToken")
            .asText();

        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RefreshTokenRequest(refreshTokenValue))))
            .andExpect(status().isNoContent());

        assertThat(refreshTokenRepository.findByToken(refreshTokenValue)).isEmpty();
    }
}