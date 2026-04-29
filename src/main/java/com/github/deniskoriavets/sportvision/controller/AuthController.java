package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.dto.response.AuthResponse;
import com.github.deniskoriavets.sportvision.dto.request.LoginRequest;
import com.github.deniskoriavets.sportvision.dto.request.RefreshTokenRequest;
import com.github.deniskoriavets.sportvision.dto.request.RegisterRequest;
import com.github.deniskoriavets.sportvision.dto.request.ResendVerificationRequest;
import com.github.deniskoriavets.sportvision.dto.response.ErrorResponse;
import com.github.deniskoriavets.sportvision.service.interfaces.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, login and session management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new parent account")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User successfully registered"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email already taken",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive JWT tokens")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully authenticated",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials or email not verified",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify email address using a token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Email successfully verified"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> verify(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid refresh token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend email verification link")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Verification email sent"),
        @ApiResponse(responseCode = "400", description = "Invalid email or already verified",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> resendVerification(
        @Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Logout and invalidate refresh token")
    @ApiResponse(responseCode = "204", description = "Successfully logged out")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}