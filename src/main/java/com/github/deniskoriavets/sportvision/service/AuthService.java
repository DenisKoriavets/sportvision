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
import com.github.deniskoriavets.sportvision.exception.EmailAlreadyVerifiedException;
import com.github.deniskoriavets.sportvision.exception.EmailNotVerifiedException;
import com.github.deniskoriavets.sportvision.exception.InvalidTokenException;
import com.github.deniskoriavets.sportvision.exception.TokenExpiredException;
import com.github.deniskoriavets.sportvision.repository.ParentRepository;
import com.github.deniskoriavets.sportvision.repository.RefreshTokenRepository;
import com.github.deniskoriavets.sportvision.repository.VerificationTokenRepository;
import com.github.deniskoriavets.sportvision.security.JwtService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ParentRepository parentRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;

    @Transactional
    public void register(RegisterRequest request) {
        if (parentRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyTakenException("Email " + request.email() + " вже зайнятий");
        }

        Parent parent = Parent.builder()
            .email(request.email())
            .passwordHash(passwordEncoder.encode(request.password()))
            .firstName(request.firstName())
            .lastName(request.lastName())
            .role(Role.PARENT)
            .isEmailVerified(false)
            .build();

        parentRepository.save(parent);

        String verificationToken = UUID.randomUUID().toString();
        saveVerificationToken(parent, verificationToken);

        emailService.sendVerificationEmail(parent.getEmail(), verificationToken);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        var parent = parentRepository.findByEmail(request.email())
            .orElseThrow();

        if (!parent.isEmailVerified()) {
            throw new EmailNotVerifiedException("Будь ласка, підтвердіть вашу пошту");
        }

        return generateAuthResponse(parent);
    }

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

    @Transactional
    public void logout(String refreshTokenRequest) {
        refreshTokenRepository.findByToken(refreshTokenRequest)
            .ifPresent(refreshTokenRepository::delete);
    }

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

    @Transactional
    public void resendVerification(String email) {
        Parent parent = parentRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Email не знайдено"));
        if (parent.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException("Email вже підтверджено");
        }
        refreshTokenRepository.deleteAllByParent(parent);
        saveVerificationToken(parent, UUID.randomUUID().toString());
        emailService.sendVerificationEmail(parent.getEmail(), email);
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

    private void saveVerificationToken(Parent parent, String token) {
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(24);

        VerificationToken verificationToken = VerificationToken.builder()
            .token(token)
            .parent(parent)
            .expiryDate(expiryDate)
            .build();

        verificationTokenRepository.save(verificationToken);
    }
}