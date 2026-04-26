package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.entity.enums.PaymentStatus;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import com.github.deniskoriavets.sportvision.event.PaymentExpiredEvent;
import com.github.deniskoriavets.sportvision.repository.PaymentRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCleanupService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expirePendingPayments() {
        log.info("Starting scheduled cleanup of pending payments...");
        
        LocalDateTime limit = LocalDateTime.now().minusHours(24);
        var expiredPayments = paymentRepository.findAllByStatusAndCreatedAtBefore(PaymentStatus.PENDING, limit);

        for (var payment : expiredPayments) {
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);

            var subscription = payment.getSubscription();
            if (subscription != null) {
                subscription.setStatus(SubscriptionStatus.CANCELLED);
                subscriptionRepository.save(subscription);
            }

            eventPublisher.publishEvent(new PaymentExpiredEvent(
                payment.getId(), 
                subscription.getChild().getId(), 
                subscription.getChild().getParent().getId()
            ));
            
            log.info("Payment {} and Subscription {} marked as expired.", payment.getId(), subscription.getId());
        }
    }
}