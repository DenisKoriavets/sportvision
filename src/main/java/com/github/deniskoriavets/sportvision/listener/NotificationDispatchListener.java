package com.github.deniskoriavets.sportvision.listener;

import com.github.deniskoriavets.sportvision.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatchListener {

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

    @EventListener
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("[NOTIFICATION HUB] | Payment SUCCESS for child: {}. Amount: {}. Plan: {}. Sending receipt...",
            event.childId(), event.amount(), event.subscriptionPlanId());
    }

    @EventListener
    public void handlePaymentExpired(PaymentExpiredEvent event) {
        log.info("[NOTIFICATION HUB] | Payment EXPIRED for child: {}. Parent: {}. Sending notification...",
            event.childId(), event.parentId());
    }

    @EventListener
    public void handleSubscriptionLow(SubscriptionLowEvent event) {
        log.info("[NOTIFICATION HUB] | Subscription LOW for parent: {}. Only {} sessions left!",
            event.parentId(), event.remainingSessions());
    }

    @EventListener
    public void handleSubscriptionExpired(SubscriptionExpiredEvent event) {
        log.info("[NOTIFICATION HUB] | Subscription EXPIRED for child: {}. Parent: {}. Sending renewal link...",
            event.childId(), event.parentId());
    }

    @EventListener
    public void handleSessionReminder(SessionReminderEvent event) {
        log.info("[NOTIFICATION HUB] | Reminder for session: {} in group: {}. Sending push notification...",
            event.sessionId(), event.groupId());
    }
}