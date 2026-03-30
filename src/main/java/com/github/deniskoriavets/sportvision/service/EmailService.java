package com.github.deniskoriavets.sportvision.service;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
}