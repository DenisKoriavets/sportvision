package com.github.deniskoriavets.sportvision.listener;

import com.github.deniskoriavets.sportvision.entity.enums.AttendanceStatus;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import com.github.deniskoriavets.sportvision.event.AttendanceMarkedEvent;
import com.github.deniskoriavets.sportvision.event.SubscriptionExpiredEvent;
import com.github.deniskoriavets.sportvision.event.SubscriptionLowEvent;
import com.github.deniskoriavets.sportvision.repository.SessionRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SubscriptionDeductionListener {

    private final SessionRepository sessionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAttendanceMarked(AttendanceMarkedEvent event) {
        if (event.status() != AttendanceStatus.PRESENT) {
            return;
        }
        var session = sessionRepository.findById(event.sessionId())
            .orElseThrow(() -> new IllegalStateException("Session not found"));
        var group = session.getGroup();
        var section = group.getSection();
        var subscription =
            subscriptionRepository.findFirstByChildIdAndSubscriptionPlanSectionIdAndStatus(
                    event.childId(), section.getId(), SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException(
                    "Active subscription not found for child and section"));
        subscription.setRemainingSessions(subscription.getRemainingSessions() - 1);
        if (subscription.getRemainingSessions() <= 0) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            eventPublisher.publishEvent(new SubscriptionExpiredEvent(
                subscription.getId(), event.childId(), subscription.getChild().getParent().getId()));
        } else if (subscription.getRemainingSessions() <= 2) {
            eventPublisher.publishEvent(new SubscriptionLowEvent(
                subscription.getId(), subscription.getChild().getParent().getId(), subscription.getRemainingSessions()));
        }
        subscriptionRepository.save(subscription);
    }
}
