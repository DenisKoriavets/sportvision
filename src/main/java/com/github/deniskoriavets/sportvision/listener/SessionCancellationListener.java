package com.github.deniskoriavets.sportvision.listener;

import com.github.deniskoriavets.sportvision.entity.enums.AttendanceStatus;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import com.github.deniskoriavets.sportvision.event.SessionCancelledEvent;
import com.github.deniskoriavets.sportvision.repository.AttendanceRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SessionCancellationListener {

    private final AttendanceRepository attendanceRepository;
    private final SubscriptionRepository subscriptionRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSessionCancelled(SessionCancelledEvent event) {
        var attendancesOnSession = attendanceRepository.findAllBySessionId(event.sessionId());
        for (var attendance : attendancesOnSession) {
            if (attendance.getStatus() == AttendanceStatus.PRESENT) {
                var subscription = subscriptionRepository.findFirstByChildIdAndSubscriptionPlanSectionIdAndStatusIn(
                        attendance.getChild().getId(),
                        attendance.getSession().getGroup().getSection().getId(),
                        List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.EXPIRED))
                    .orElseThrow(() -> new IllegalStateException(
                        "Subscription not found for child and section"));
                subscription.setRemainingSessions(subscription.getRemainingSessions() + 1);
                if (subscription.getStatus() == SubscriptionStatus.EXPIRED) {
                    subscription.setStatus(SubscriptionStatus.ACTIVE);
                }
                subscriptionRepository.save(subscription);
            }
        }
    }
}
