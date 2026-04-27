package com.github.deniskoriavets.sportvision.notification;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
}