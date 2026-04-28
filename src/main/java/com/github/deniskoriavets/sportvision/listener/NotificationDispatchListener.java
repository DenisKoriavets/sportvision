package com.github.deniskoriavets.sportvision.listener;

import com.github.deniskoriavets.sportvision.dto.NotificationMessage;
import com.github.deniskoriavets.sportvision.entity.enums.NotificationPreference;
import com.github.deniskoriavets.sportvision.event.*;
import com.github.deniskoriavets.sportvision.exception.ResourceNotFoundException;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.repository.ParentRepository;
import com.github.deniskoriavets.sportvision.repository.SessionRepository;
import com.github.deniskoriavets.sportvision.notification.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatchListener {

    private final ChildRepository childRepository;
    private final NotificationDispatcher notificationDispatcher;
    private final ParentRepository parentRepository;
    private final SessionRepository sessionRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAttendanceMarked(AttendanceMarkedEvent event) {
        log.info("[NOTIFICATION HUB] | Attendance marked for child: {} in session: {}. Status: {}",
            event.childId(), event.sessionId(), event.status());

        try {
            var session = sessionRepository.findById(event.sessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
            var child = childRepository.findById(event.childId())
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));
            var parent = child.getParent();

            String subject = "SportVision - Відвідування тренування";

            String telegramText = String.format(
                "Відмічено статус відвідування для вашої дитини (%s).\nСекція: %s\nДата: %s\nСтатус: %s",
                child.getFirstName(), session.getGroup().getName(), session.getDate().toString(),
                event.status().name());

            Map<String, Object> vars = Map.of(
                "parentName", parent.getFirstName(),
                "childName", child.getFirstName(),
                "sectionName", session.getGroup().getName(),
                "date", session.getDate().toString(),
                "status", event.status().name()
            );

            var msg = new NotificationMessage(
                parent.getEmail(),
                parent.getTelegramChatId(),
                subject,
                telegramText,
                "attendance-marked",
                vars
            );

            dispatchSafely(parent.getNotificationPreferences(), msg);
        } catch (Exception e) {
            log.error("[NOTIFICATION HUB] | Failed to send attendance notification: {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSessionCancelled(SessionCancelledEvent event) {
        try {
            var session = sessionRepository.findById(event.sessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
            var children = childRepository.findAllByGroupId(session.getGroup().getId());

            for (var child : children) {
                var parent = child.getParent();
                String subject = "SportVision - Скасування тренування!";

                String telegramText =
                    String.format("Тренування для %s у секції %s на %s %s скасовано.\nПричина: %s",
                        child.getFirstName(), session.getGroup().getName(),
                        session.getDate().toString(), session.getStartTime().toString(),
                        event.reason());

                Map<String, Object> vars = Map.of(
                    "parentName", parent.getFirstName(),
                    "childName", child.getFirstName(),
                    "sectionName", session.getGroup().getName(),
                    "date", session.getDate().toString(),
                    "time", session.getStartTime().toString(),
                    "reason", event.reason() != null ? event.reason() : ""
                );

                var msg =
                    new NotificationMessage(parent.getEmail(), parent.getTelegramChatId(), subject,
                        telegramText, "session-cancelled", vars);
                dispatchSafely(parent.getNotificationPreferences(), msg);
            }
        } catch (Exception e) {
            log.error("[NOTIFICATION HUB] | Failed to send session cancellation: {}",
                e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        try {
            var child = childRepository.findById(event.childId())
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));
            var parent = child.getParent();
            String subject = "SportVision - Успішна оплата абонемента!";

            String telegramText = String.format(
                "Оплата в розмірі %d UAH за абонемент для %s успішно пройшла. Дякуємо!",
                event.amount(), child.getFirstName());
            Map<String, Object> vars = Map.of(
                "firstName", parent.getFirstName(),
                "childName", child.getFirstName(),
                "amount", event.amount()
            );

            var msg =
                new NotificationMessage(parent.getEmail(), parent.getTelegramChatId(), subject,
                    telegramText, "payment-success", vars);
            dispatchSafely(parent.getNotificationPreferences(), msg);
        } catch (Exception e) {
            log.error("[NOTIFICATION HUB] | Failed to send payment success notification: {}",
                e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentExpired(PaymentExpiredEvent event) {
        try {
            var parent = parentRepository.findById(event.parentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));
            var child = childRepository.findById(event.childId())
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

            String subject = "SportVision - Платіж скасовано";

            String telegramText = String.format(
                "Ваш платіж для дитини %s не був завершений вчасно і його скасовано системою.",
                child.getFirstName());

            Map<String, Object> vars = Map.of(
                "parentName", parent.getFirstName(),
                "childName", child.getFirstName()
            );

            var msg = new NotificationMessage(
                parent.getEmail(),
                parent.getTelegramChatId(),
                subject,
                telegramText,
                "payment-expired",
                vars
            );

            dispatchSafely(parent.getNotificationPreferences(), msg);
        } catch (Exception e) {
            log.error("[NOTIFICATION HUB] | Failed to send payment expired notification: {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSubscriptionLow(SubscriptionLowEvent event) {
        try {
            var parent = parentRepository.findById(event.parentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));
            String subject = "SportVision - Закінчується абонемент!";

            String telegramText = String.format(
                "На вашому балансі залишилося всього %d занять. Будь ласка, подбайте про поповнення рахунку заздалегідь.",
                event.remainingSessions());
            Map<String, Object> vars = Map.of(
                "parentName", parent.getFirstName(),
                "childName", "вашої дитини",
                "remainingSessions", event.remainingSessions()
            );

            var msg =
                new NotificationMessage(parent.getEmail(), parent.getTelegramChatId(), subject,
                    telegramText, "subscription-low", vars);
            dispatchSafely(parent.getNotificationPreferences(), msg);
        } catch (Exception e) {
            log.error("[NOTIFICATION HUB] | Failed to send subscription low notification: {}",
                e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSubscriptionExpired(SubscriptionExpiredEvent event) {
        try {
            var parent = parentRepository.findById(event.parentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));
            var child = childRepository.findById(event.childId())
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));
            String subject = "SportVision - Абонемент закінчився";

            String telegramText =
                String.format("Абонемент для вашої дитини (%s) повністю вичерпано.",
                    child.getFirstName());
            Map<String, Object> vars = Map.of(
                "parentName", parent.getFirstName(),
                "childName", child.getFirstName()
            );

            var msg =
                new NotificationMessage(parent.getEmail(), parent.getTelegramChatId(), subject,
                    telegramText, "subscription-expired", vars);
            dispatchSafely(parent.getNotificationPreferences(), msg);
        } catch (Exception e) {
            log.error("[NOTIFICATION HUB] | Failed to send subscription expired notification: {}",
                e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSessionReminder(SessionReminderEvent event) {
        try {
            var session = sessionRepository.findById(event.sessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
            var children = childRepository.findAllByGroupId(session.getGroup().getId());

            for (var child : children) {
                var parent = child.getParent();
                String subject = "SportVision - Нагадування про тренування!";

                String telegramText = String.format(
                    "Нагадуємо, що завтра (%s) о %s відбудеться тренування для вашої дитини (%s) у секції %s.",
                    session.getDate().toString(), session.getStartTime().toString(),
                    child.getFirstName(), session.getGroup().getName());

                Map<String, Object> vars = Map.of(
                    "parentName", parent.getFirstName(),
                    "childName", child.getFirstName(),
                    "sectionName", session.getGroup().getName(),
                    "date", session.getDate().toString(),
                    "time", session.getStartTime().toString()
                );

                var msg =
                    new NotificationMessage(parent.getEmail(), parent.getTelegramChatId(), subject,
                        telegramText, "session-reminder", vars);
                dispatchSafely(parent.getNotificationPreferences(), msg);
            }
        } catch (Exception e) {
            log.error("[NOTIFICATION HUB] | Failed to send session reminders: {}", e.getMessage());
        }
    }

    private void dispatchSafely(Set<NotificationPreference> preferences,
                                NotificationMessage message) {
        Set<NotificationPreference> safePreferences = (preferences == null || preferences.isEmpty())
            ? Set.of(NotificationPreference.EMAIL)
            : preferences;

        notificationDispatcher.dispatch(safePreferences, message);
    }
}