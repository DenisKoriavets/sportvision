package com.github.deniskoriavets.sportvision.listener;

import com.github.deniskoriavets.sportvision.dto.NotificationMessage;
import com.github.deniskoriavets.sportvision.entity.enums.NotificationPreference;
import com.github.deniskoriavets.sportvision.event.*;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.service.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatchListener {

    private final ChildRepository childRepository;
    private final NotificationDispatcher notificationDispatcher;

    @EventListener
    public void handleAttendanceMarked(AttendanceMarkedEvent event) {
        log.info("[NOTIFICATION HUB] | Attendance marked for child: {} in session: {}. Status: {}",
            event.childId(), event.sessionId(), event.status());
    }

    @EventListener
    public void handleSessionCancelled(SessionCancelledEvent event) {
        log.info("[NOTIFICATION HUB] | Session {} CANCELLED. Reason: {}. Notifying all parents...",
            event.sessionId(), event.reason());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info(
            "[NOTIFICATION HUB] | Payment SUCCESS for child: {}. Amount: {}. Plan: {}. Sending receipt...",
            event.childId(), event.amount(), event.subscriptionPlanId());

        try {
            var child = childRepository.findById(event.childId())
                .orElseThrow(() -> new RuntimeException("Child not found"));
            var parent = child.getParent();

            String subject = "SportVision - Успішна оплата абонемента!";
            String content = String.format(
                "Шановний/а %s,<br><br>Оплата в розмірі <b>%d UAH</b> за абонемент для <b>%s</b> успішно пройшла.<br>Дякуємо, що ви з нами!",
                parent.getFirstName(), event.amount(), child.getFirstName()
            );
            NotificationMessage message = new NotificationMessage(
                parent.getEmail(),
                subject,
                content
            );

            notificationDispatcher.dispatch(NotificationPreference.EMAIL, message);
        } catch (Exception e) {
            log.error("[NOTIFICATION HUB] | Failed to send payment receipt for child {}: {}",
                event.childId(), e.getMessage());
        }
    }

    @EventListener
    public void handlePaymentExpired(PaymentExpiredEvent event) {
        log.info(
            "[NOTIFICATION HUB] | Payment EXPIRED for child: {}. Parent: {}. Sending notification...",
            event.childId(), event.parentId());
    }

    @EventListener
    public void handleSubscriptionLow(SubscriptionLowEvent event) {
        log.info("[NOTIFICATION HUB] | Subscription LOW for parent: {}. Only {} sessions left!",
            event.parentId(), event.remainingSessions());
    }

    @EventListener
    public void handleSubscriptionExpired(SubscriptionExpiredEvent event) {
        log.info(
            "[NOTIFICATION HUB] | Subscription EXPIRED for child: {}. Parent: {}. Sending renewal link...",
            event.childId(), event.parentId());
    }

    @EventListener
    public void handleSessionReminder(SessionReminderEvent event) {
        log.info(
            "[NOTIFICATION HUB] | Reminder for session: {} in group: {}. Sending push notification...",
            event.sessionId(), event.groupId());
    }
}