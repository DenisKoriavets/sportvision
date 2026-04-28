package com.github.deniskoriavets.sportvision.notification;

import java.util.Map;

public interface EmailService {
    void sendVerificationEmail(String to, String token);

    void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables);
}