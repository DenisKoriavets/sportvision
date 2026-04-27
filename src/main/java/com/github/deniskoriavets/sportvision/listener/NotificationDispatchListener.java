package com.github.deniskoriavets.sportvision.listener;

import com.github.deniskoriavets.sportvision.dto.NotificationMessage;
import com.github.deniskoriavets.sportvision.entity.Parent;
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
            String content = String.format(
                "Шановний/а %s!<br><br>Відмічено статус відвідування для вашої дитини (<b>%s</b>).<br>Секція: <b>%s</b><br>Дата тренування: <b>%s</b><br>Статус: <b>%s</b>.",
                parent.getFirstName(), child.getFirstName(), session.getGroup().getName(),
                session.getDate().toString(), event.status().name()
            );

            sendToAllPreferences(parent, subject, content);
        } catch (Exception e) {
            log.error("[NOTIFICATION HUB] | Failed to send attendance notification: {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSessionCancelled(SessionCancelledEvent event) {
        log.info("[NOTIFICATION HUB] | Session {} CANCELLED. Reason: {}. Notifying all parents...",
            event.sessionId(), event.reason());

        try {
            var session = sessionRepository.findById(event.sessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found for notification"));

            var children = childRepository.findAllByGroupId(session.getGroup().getId());

            for (var child : children) {
                var parent = child.getParent();

                String subject = "SportVision - Скасування тренування!";
                String content = String.format(
                    "Шановний/а %s!<br><br>Тренування для вашої дитини (<b>%s</b>) у секції <b>%s</b>, заплановане на <b>%s %s</b>, було скасовано.<br>Причина: <i>%s</i>.<br><br>Перепрошуємо за незручності.",
                    parent.getFirstName(), child.getFirstName(), session.getGroup().getName(),
                    session.getDate().toString(), session.getStartTime().toString(), event.reason()
                );

                sendToAllPreferences(parent, subject, content);
            }
        } catch (Exception e) {
            log.error("[NOTIFICATION HUB] | Failed to send session cancellation notification: {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("[NOTIFICATION HUB] | Payment SUCCESS for child: {}. Amount: {}.",
            event.childId(), event.amount());

        try {
            var child = childRepository.findById(event.childId())
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));
            var parent = child.getParent();

            String subject = "SportVision - Успішна оплата абонемента!";
            String content = String.format(
                "Шановний/а %s,<br><br>Оплата в розмірі <b>%d UAH</b> за абонемент для <b>%s</b> успішно пройшла.<br>Дякуємо, що ви з нами!",
                parent.getFirstName(), event.amount(), child.getFirstName()
            );

            sendToAllPreferences(parent, subject, content);
        } catch (Exception e) {
            log.error("[NOTIFICATION HUB] | Failed to send payment success notification: {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentExpired(PaymentExpiredEvent event) {
        log.info("[NOTIFICATION HUB] | Payment EXPIRED for child: {}. Parent: {}.",
            event.childId(), event.parentId());

        try {
            var parent = parentRepository.findById(event.parentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));
            var child = childRepository.findById(event.childId())
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

            String subject = "SportVision - Платіж скасовано";
            String content = String.format(
                "Шановний/а %s,<br><br>Ваш платіж для дитини <b>%s</b> не був завершений вчасно і його скасовано системою.<br>Будь ласка, спробуйте ще раз або зверніться до адміністратора.",
                parent.getFirstName(), child.getFirstName()
            );

            sendToAllPreferences(parent, subject, content);
        } catch (Exception e) {
            log.error("[NOTIFICATION HUB] | Failed to send payment expired notification: {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSubscriptionLow(SubscriptionLowEvent event) {
        log.info("[NOTIFICATION HUB] | Subscription LOW for parent: {}. Only {} sessions left!",
            event.parentId(), event.remainingSessions());

        try {
            var parent = parentRepository.findById(event.parentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));

            String subject = "SportVision - Закінчується абонемент!";
            String content = String.format(
                "Шановний/а %s!<br><br>Повідомляємо, що на вашому балансі залишилося всього <b>%d</b> занять.<br>Будь ласка, подбайте про поповнення рахунку заздалегідь.",
                parent.getFirstName(), event.remainingSessions()
            );

            sendToAllPreferences(parent, subject, content);
        } catch (Exception e) {
            log.error("[NOTIFICATION HUB] | Failed to send subscription low notification: {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSubscriptionExpired(SubscriptionExpiredEvent event) {
        log.info("[NOTIFICATION HUB] | Subscription EXPIRED for child: {}. Parent: {}.",
            event.childId(), event.parentId());

        try {
            var parent = parentRepository.findById(event.parentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));
            var child = childRepository.findById(event.childId())
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

            String subject = "SportVision - Абонемент закінчився";
            String content = String.format(
                "Шановний/а %s!<br><br>Абонемент для вашої дитини (<b>%s</b>) повністю вичерпано.<br>Будь ласка, придбайте новий абонемент для продовження тренувань.",
                parent.getFirstName(), child.getFirstName()
            );

            sendToAllPreferences(parent, subject, content);
        } catch (Exception e) {
            log.error("[NOTIFICATION HUB] | Failed to send subscription expired notification: {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSessionReminder(SessionReminderEvent event) {
        log.info("[NOTIFICATION HUB] | Reminder for session: {} in group: {}. Notifying parents...",
            event.sessionId(), event.groupId());

        try {
            var session = sessionRepository.findById(event.sessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

            var children = childRepository.findAllByGroupId(session.getGroup().getId());

            for (var child : children) {
                var parent = child.getParent();

                String subject = "SportVision - Нагадування про тренування!";
                String content = String.format(
                    "Шановний/а %s!<br><br>Нагадуємо, що завтра (<b>%s</b>) о <b>%s</b> відбудеться тренування для вашої дитини (<b>%s</b>) у секції <b>%s</b>.",
                    parent.getFirstName(), session.getDate().toString(), session.getStartTime().toString(), child.getFirstName(), session.getGroup().getName()
                );

                sendToAllPreferences(parent, subject, content);
            }
        } catch (Exception e) {
            log.error("[NOTIFICATION HUB] | Failed to send session reminders: {}", e.getMessage());
        }
    }

    private void sendToAllPreferences(Parent parent, String subject, String content) {
        if (parent.getNotificationPreferences() == null || parent.getNotificationPreferences().isEmpty()) {
            notificationDispatcher.dispatch(NotificationPreference.EMAIL,
                new NotificationMessage(parent.getEmail(), subject, content));
            return;
        }

        for (NotificationPreference pref : parent.getNotificationPreferences()) {

            String recipient = (pref == NotificationPreference.TELEGRAM) ? parent.getTelegramChatId() : parent.getEmail();

            if (recipient != null && !recipient.isBlank()) {
                notificationDispatcher.dispatch(pref, new NotificationMessage(recipient, subject, content));
            } else {
                log.warn("Cannot send {} notification to parent {} - recipient address/id is missing", pref, parent.getId());
            }
        }
    }
}