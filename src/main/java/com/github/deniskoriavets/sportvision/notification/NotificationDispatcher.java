package com.github.deniskoriavets.sportvision.notification;

import com.github.deniskoriavets.sportvision.dto.NotificationMessage;
import com.github.deniskoriavets.sportvision.entity.enums.NotificationPreference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcher {

    private final List<NotificationStrategy> strategies;

    public void dispatch(Set<NotificationPreference> preferences, NotificationMessage message) {
        if (preferences == null || preferences.isEmpty()) {
            log.debug("No notification preferences set for recipient: {}", message.email());
            return;
        }

        preferences.forEach(preference -> {
            strategies.stream()
                .filter(strategy -> strategy.supports(preference))
                .forEach(strategy -> {
                    try {
                        strategy.send(message);
                    } catch (Exception e) {
                        log.error("Failed to send notification via {}: {}",
                            strategy.getClass().getSimpleName(), e.getMessage());
                    }
                });
        });
    }
}