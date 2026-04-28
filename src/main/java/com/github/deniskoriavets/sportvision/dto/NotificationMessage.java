package com.github.deniskoriavets.sportvision.dto;

import java.util.Map;

public record NotificationMessage(
    String email,
    String telegramChatId,
    String subject,
    String content,
    String templateName,
    Map<String, Object> templateVariables
) {

    public NotificationMessage(String email, String telegramChatId, String subject,
                               String content) {
        this(email, telegramChatId, subject, content, null, null);
    }
}