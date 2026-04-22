package com.github.deniskoriavets.sportvision.service.interfaces;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
}