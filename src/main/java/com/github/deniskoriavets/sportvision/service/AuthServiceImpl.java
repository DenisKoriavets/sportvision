package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.config.JwtProperties;
import com.github.deniskoriavets.sportvision.dto.AuthResponse;
import com.github.deniskoriavets.sportvision.dto.LoginRequest;
import com.github.deniskoriavets.sportvision.dto.RegisterRequest;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.RefreshToken;
import com.github.deniskoriavets.sportvision.entity.VerificationToken;
import com.github.deniskoriavets.sportvision.entity.enums.Role;
import com.github.deniskoriavets.sportvision.exception.EmailAlreadyTakenException;
import com.github.deniskoriavets.sportvision.exception.EmailNotVerifiedException;
import com.github.deniskoriavets.sportvision.exception.InvalidCredentialsException;
import com.github.deniskoriavets.sportvision.exception.InvalidTokenException;
import com.github.deniskoriavets.sportvision.exception.ResourceNotFoundException;
import com.github.deniskoriavets.sportvision.exception.TokenExpiredException;
import com.github.deniskoriavets.sportvision.repository.ParentRepository;
import com.github.deniskoriavets.sportvision.repository.RefreshTokenRepository;
import com.github.deniskoriavets.sportvision.repository.VerificationTokenRepository;
import com.github.deniskoriavets.sportvision.security.JwtService;
import com.github.deniskoriavets.sportvision.service.interfaces.AuthService;
import com.github.deniskoriavets.sportvision.service.interfaces.EmailService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final ParentRepository parentRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        try {
            Parent parent = Parent.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.PARENT)
                .build();

            Parent savedParent = parentRepository.save(parent);

            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .parent(savedParent)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

            verificationTokenRepository.save(verificationToken);
            emailService.sendVerificationEmail(savedParent.getEmail(), token);

        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyTakenException("Email " + request.email() + " is already registered");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Parent parent = parentRepository.findByEmail(request.email())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), parent.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!parent.isEmailVerified()) {
            throw new EmailNotVerifiedException("Please verify your email first");
        }

        return generateAuthResponse(parent);
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshTokenRequest) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenRequest)
            .orElseThrow(() -> new InvalidTokenException("Невірний Refresh Token"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now()) || refreshToken.isRevoked()) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiredException("Refresh токен прострочений або відкликаний");
        }

        Parent parent = refreshToken.getParent();
        refreshTokenRepository.delete(refreshToken);

        return generateAuthResponse(parent);
    }

    @Override
    @Transactional
    public void logout(String refreshTokenRequest) {
        refreshTokenRepository.findByToken(refreshTokenRequest)
            .ifPresent(refreshTokenRepository::delete);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
            .orElseThrow(() -> new InvalidTokenException(
                "Токен підтвердження не знайдено або він невірний"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(verificationToken);
            throw new TokenExpiredException("Термін дії посилання для підтвердження минув");
        }

        Parent parent = verificationToken.getParent();
        parent.setEmailVerified(true);
        parentRepository.save(parent);

        verificationTokenRepository.delete(verificationToken);
    }

    @Override
    @Transactional
    public void resendVerification(String email) {
        Parent parent = parentRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (parent.isEmailVerified()) {
            throw new IllegalStateException("Email is already verified");
        }

        verificationTokenRepository.deleteByParent(parent);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
            .token(token)
            .parent(parent)
            .expiryDate(LocalDateTime.now().plusHours(24))
            .build();

        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(parent.getEmail(), token);
    }

    private AuthResponse generateAuthResponse(Parent parent) {
        String accessToken = jwtService.generateAccessToken(parent);
        String refreshTokenStr = jwtService.generateRefreshToken(parent);

        RefreshToken refreshToken = RefreshToken.builder()
            .token(refreshTokenStr)
            .parent(parent)
            .expiryDate(Instant.now().plusMillis(jwtProperties.getRefreshToken().getExpiration()))
            .build();
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, refreshTokenStr);
    }
}