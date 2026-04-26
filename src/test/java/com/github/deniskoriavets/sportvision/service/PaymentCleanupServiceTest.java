package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.entity.Child;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.Payment;
import com.github.deniskoriavets.sportvision.entity.Subscription;
import com.github.deniskoriavets.sportvision.entity.enums.PaymentStatus;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import com.github.deniskoriavets.sportvision.event.PaymentExpiredEvent;
import com.github.deniskoriavets.sportvision.repository.PaymentRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCleanupServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentCleanupService paymentCleanupService;

    @Test
    @DisplayName("Marks expired pending payment and its subscription as cancelled")
    void expirePendingPayments_SetsExpiredStatusOnPaymentAndSubscription() {
        var parentId = UUID.randomUUID();
        var childId = UUID.randomUUID();

        var parent = new Parent();
        parent.setId(parentId);

        var child = new Child();
        child.setId(childId);
        child.setParent(parent);

        var subscription = Subscription.builder()
            .id(UUID.randomUUID())
            .status(SubscriptionStatus.PENDING_PAYMENT)
            .child(child)
            .build();

        var payment = Payment.builder()
            .id(UUID.randomUUID())
            .amount(BigDecimal.valueOf(1000))
            .status(PaymentStatus.PENDING)
            .createdAt(LocalDateTime.now().minusHours(2))
            .subscription(subscription)
            .build();

        when(paymentRepository.findAllByStatusAndCreatedAtBefore(eq(PaymentStatus.PENDING), any()))
            .thenReturn(List.of(payment));

        paymentCleanupService.expirePendingPayments();

        assertEquals(PaymentStatus.EXPIRED, payment.getStatus());
        assertEquals(SubscriptionStatus.CANCELLED, subscription.getStatus());

        verify(paymentRepository).save(payment);
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    @DisplayName("Publishes PaymentExpiredEvent with correct data for each expired payment")
    void expirePendingPayments_PublishesPaymentExpiredEvent() {
        var parentId = UUID.randomUUID();
        var childId = UUID.randomUUID();
        var paymentId = UUID.randomUUID();

        var parent = new Parent();
        parent.setId(parentId);

        var child = new Child();
        child.setId(childId);
        child.setParent(parent);

        var subscription = Subscription.builder()
            .id(UUID.randomUUID())
            .status(SubscriptionStatus.PENDING_PAYMENT)
            .child(child)
            .build();

        var payment = Payment.builder()
            .id(paymentId)
            .amount(BigDecimal.valueOf(500))
            .status(PaymentStatus.PENDING)
            .createdAt(LocalDateTime.now().minusHours(2))
            .subscription(subscription)
            .build();

        when(paymentRepository.findAllByStatusAndCreatedAtBefore(eq(PaymentStatus.PENDING), any()))
            .thenReturn(List.of(payment));

        paymentCleanupService.expirePendingPayments();

        var eventCaptor = ArgumentCaptor.forClass(PaymentExpiredEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        var publishedEvent = eventCaptor.getValue();
        assertEquals(paymentId, publishedEvent.paymentId());
        assertEquals(childId, publishedEvent.childId());
        assertEquals(parentId, publishedEvent.parentId());
    }

    @Test
    @DisplayName("Does nothing when there are no expired pending payments")
    void expirePendingPayments_DoesNothing_WhenNoExpiredPaymentsFound() {
        when(paymentRepository.findAllByStatusAndCreatedAtBefore(eq(PaymentStatus.PENDING), any()))
            .thenReturn(List.of());

        paymentCleanupService.expirePendingPayments();

        verify(paymentRepository, org.mockito.Mockito.never()).save(any());
        verify(subscriptionRepository, org.mockito.Mockito.never()).save(any());
        verify(eventPublisher, org.mockito.Mockito.never()).publishEvent(any());
    }
}