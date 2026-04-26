package com.github.deniskoriavets.sportvision.scheduler;

import com.github.deniskoriavets.sportvision.entity.enums.SessionStatus;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import com.github.deniskoriavets.sportvision.event.SessionReminderEvent;
import com.github.deniskoriavets.sportvision.event.SubscriptionLowEvent;
import com.github.deniskoriavets.sportvision.repository.SessionRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final SessionRepository sessionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SubscriptionRepository subscriptionRepository;

    @Scheduled(cron = "0 0 18 * * *")
    public void scheduleSessionReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        var tomorrowSessions =
            sessionRepository.findAllByDateAndStatus(tomorrow, SessionStatus.SCHEDULED);
        tomorrowSessions.forEach(session -> {
            eventPublisher.publishEvent(
                new SessionReminderEvent(session.getId(), session.getGroup().getId()));
        });
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void scheduleSubscriptionExpiryReminders() {
        var expiringSubscriptions =
            subscriptionRepository.findAllByStatusAndRemainingSessionsLessThanEqual(
                SubscriptionStatus.ACTIVE, 2
            );
        expiringSubscriptions.forEach(subscription -> {
            eventPublisher.publishEvent(new SubscriptionLowEvent(subscription.getId(),
                subscription.getChild().getParent().getId(), subscription.getRemainingSessions()));
        });
    }
}
