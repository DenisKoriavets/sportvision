package com.github.deniskoriavets.sportvision.notification;

import com.github.deniskoriavets.sportvision.dto.NotificationMessage;
import com.github.deniskoriavets.sportvision.entity.enums.NotificationPreference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcher {

    private final List<NotificationStrategy> strategies;

    public void dispatch(NotificationPreference preference, NotificationMessage message) {
        strategies.stream()
            .filter(strategy -> strategy.supports(preference))
            .findFirst()
            .ifPresentOrElse(
                strategy -> strategy.send(message),
                () -> log.warn("No notification strategy found for preference: {}", preference)
            );
    }
}