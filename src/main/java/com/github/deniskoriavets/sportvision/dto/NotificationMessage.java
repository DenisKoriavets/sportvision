package com.github.deniskoriavets.sportvision.dto;

public record NotificationMessage(
    String recipient,
    String subject,
    String content
) {
}
