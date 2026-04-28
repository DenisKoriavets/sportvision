package com.github.deniskoriavets.sportvision.notification;

import com.github.deniskoriavets.sportvision.dto.NotificationMessage;
import com.github.deniskoriavets.sportvision.entity.enums.NotificationPreference;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramNotificationService implements NotificationStrategy {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.telegram.bot-token:default_token_for_tests}")
    private String botToken;

    @Override
    public boolean supports(NotificationPreference preference) {
        return preference == NotificationPreference.TELEGRAM;
    }

    @Override
    public void send(NotificationMessage message) {
        if (message.telegramChatId() == null || message.telegramChatId().isBlank()) return;

        String telegramApiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        Map<String, String> requestBody = Map.of(
            "chat_id", message.telegramChatId(),
            "text", "<b>" + message.subject() + "</b>\n\n" + message.content(),
            "parse_mode", "HTML"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.postForEntity(telegramApiUrl, requestEntity, String.class);
            log.info("Telegram notification sent successfully to chat_id: {}", message.telegramChatId());
        } catch (Exception e) {
            log.error("Failed to send Telegram message to chat_id {}: {}", message.telegramChatId(), e.getMessage());
        }
    }
}
