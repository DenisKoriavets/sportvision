package com.github.deniskoriavets.sportvision.dto;

public record LoginRequest(
    String email,
    String password
) {}