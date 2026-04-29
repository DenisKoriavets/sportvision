package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.request.PaymentRequest;
import com.github.deniskoriavets.sportvision.dto.request.SubscriptionRequest;
import com.github.deniskoriavets.sportvision.dto.response.PaymentDetailResponse;
import com.github.deniskoriavets.sportvision.dto.response.PaymentResponse;
import com.github.deniskoriavets.sportvision.dto.response.SubscriptionResponse;
import com.github.deniskoriavets.sportvision.entity.*;
import com.github.deniskoriavets.sportvision.entity.enums.PaymentStatus;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import com.github.deniskoriavets.sportvision.event.PaymentSuccessEvent;
import com.github.deniskoriavets.sportvision.mapper.SubscriptionMapper;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.repository.PaymentRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionPlanRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionRepository;
import com.github.deniskoriavets.sportvision.security.SecurityFacade;
import com.github.deniskoriavets.sportvision.service.interfaces.PaymentGateway;
import com.stripe.exception.StripeException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private SubscriptionMapper subscriptionMapper;
    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;
    @Mock
    private ChildRepository childRepository;
    @Mock
    private SecurityFacade securityFacade;
    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @Test
    @DisplayName("Purchases manual subscription successfully with valid data")
    void buySubscriptionManual_Success() {
        UUID childId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        SubscriptionRequest request = new SubscriptionRequest(childId, planId);

        Child child = new Child();
        child.setId(childId);

        Section section = new Section();
        section.setId(sectionId);

        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setId(planId);
        plan.setActive(true);
        plan.setSection(section);
        plan.setSessionsCount(8);
        plan.setValidityDays(30);
        plan.setPrice(BigDecimal.valueOf(1000));

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(subscriptionPlanRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(subscriptionRepository.existsByChildIdAndSubscriptionPlanSectionIdAndStatusIn(any(),
            any(), any())).thenReturn(false);
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(
            i -> i.getArguments()[0]);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);
        when(subscriptionMapper.toResponse(any())).thenReturn(mock(SubscriptionResponse.class));

        assertNotNull(subscriptionService.buySubscriptionManual(request));

        verify(subscriptionRepository, times(2)).save(any(Subscription.class));
        verify(paymentRepository).save(any(Payment.class));
        verify(eventPublisher).publishEvent(any(PaymentSuccessEvent.class));
    }

    @Test
    @DisplayName("Throws exception when subscription plan is inactive")
    void buySubscriptionManual_ThrowsException_WhenPlanInactive() {
        UUID childId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        SubscriptionRequest request = new SubscriptionRequest(childId, planId);

        Child child = new Child();

        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setActive(false);

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(subscriptionPlanRepository.findById(planId)).thenReturn(Optional.of(plan));

        assertThrows(IllegalStateException.class,
            () -> subscriptionService.buySubscriptionManual(request));
    }

    @Test
    @DisplayName("Throws exception when child already has an active subscription for this section")
    void buySubscriptionManual_ThrowsException_WhenAlreadyHasActive() {
        UUID childId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        SubscriptionRequest request = new SubscriptionRequest(childId, planId);

        Child child = new Child();

        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setActive(true);
        plan.setSection(new Section());

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(subscriptionPlanRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(subscriptionRepository.existsByChildIdAndSubscriptionPlanSectionIdAndStatusIn(any(),
            any(), any())).thenReturn(true);

        assertThrows(IllegalStateException.class,
            () -> subscriptionService.buySubscriptionManual(request));
    }

    @Test
    @DisplayName("Initiates payment successfully")
    void initiatePayment_Success() throws StripeException {
        var childId = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var sectionId = UUID.randomUUID();
        var parentId = UUID.randomUUID();

        var paymentRequest = new PaymentRequest(planId, childId);

        var parent = new Parent();
        parent.setId(parentId);

        var child = new Child();
        child.setId(paymentRequest.childId());
        child.setParent(parent);

        var plan = SubscriptionPlan.builder()
            .id(paymentRequest.subscriptionPlanId())
            .name("Test Plan")
            .price(BigDecimal.valueOf(1000))
            .validityDays(30)
            .sessionsCount(8)
            .isActive(true)
            .section(Section.builder().id(sectionId).build())
            .build();

        when(securityFacade.getCurrentUserId()).thenReturn(parentId);

        when(childRepository.findById(paymentRequest.childId())).thenReturn(Optional.of(child));
        when(subscriptionPlanRepository.findById(paymentRequest.subscriptionPlanId())).thenReturn(
            Optional.of(plan));

        when(subscriptionRepository.existsByChildIdAndSubscriptionPlanSectionIdAndStatusIn(any(),
            any(), any())).thenReturn(false);
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription savedSubscription = invocation.getArgument(0);
            savedSubscription.setId(UUID.randomUUID());
            return savedSubscription;
        });
        when(paymentGateway.createPaymentSession(anyLong(), anyString(), anyString(), anyMap()))
            .thenReturn(new PaymentResponse("https://checkout.stripe.com/...", "cs_test_123"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);

        var response = subscriptionService.initiatePayment(paymentRequest);

        assertNotNull(response);
        assertEquals("cs_test_123", response.sessionId());
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentGateway, times(1)).createPaymentSession(anyLong(), anyString(), anyString(),
            anyMap());
    }

    @Test
    @DisplayName("Completes payment successfully and activates subscription")
    void completePayment_Success() {
        String stripeSessionId = "cs_test_123";

        Child child = new Child();
        child.setId(UUID.randomUUID());

        SubscriptionPlan plan = SubscriptionPlan.builder()
            .id(UUID.randomUUID())
            .validityDays(30)
            .build();

        Subscription subscription = Subscription.builder()
            .status(SubscriptionStatus.PENDING_PAYMENT)
            .subscriptionPlan(plan)
            .child(child)
            .build();

        Payment payment = Payment.builder()
            .id(UUID.randomUUID())
            .amount(BigDecimal.valueOf(1000))
            .status(PaymentStatus.PENDING)
            .subscription(subscription)
            .build();

        when(paymentRepository.findByStripeSessionId(stripeSessionId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArguments()[0]);

        subscriptionService.completePayment(stripeSessionId);

        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        assertNotNull(subscription.getStartDate());
        assertNotNull(subscription.getEndDate());

        verify(paymentRepository).save(payment);
        verify(subscriptionRepository).save(subscription);
        verify(eventPublisher).publishEvent(any(PaymentSuccessEvent.class));
    }

    @Test
    @DisplayName("getMyPayments returns only payments of current parent")
    void getMyPayments_ReturnsOnlyCurrentParentPayments() {
        UUID parentId = UUID.randomUUID();

        Subscription subscription = Subscription.builder()
            .id(UUID.randomUUID())
            .build();

        Payment payment = Payment.builder()
            .id(UUID.randomUUID())
            .amount(BigDecimal.valueOf(500))
            .status(PaymentStatus.PAID)
            .subscription(subscription)
            .build();

        when(securityFacade.getCurrentUserId()).thenReturn(parentId);
        when(paymentRepository.findAllByParentId(parentId)).thenReturn(List.of(payment));

        List<PaymentDetailResponse> result = subscriptionService.getMyPayments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).subscriptionId()).isEqualTo(subscription.getId());
        verify(paymentRepository).findAllByParentId(parentId);
    }

    @Test
    @DisplayName("activateSubscriptionAdmin activates PENDING_PAYMENT subscription")
    void activateSubscriptionAdmin_Activates_WhenPendingPayment() {
        UUID id = UUID.randomUUID();
        Subscription sub = Subscription.builder()
            .id(id).status(SubscriptionStatus.PENDING_PAYMENT).build();

        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(sub)).thenReturn(sub);
        when(subscriptionMapper.toResponse(sub)).thenReturn(mock(SubscriptionResponse.class));

        subscriptionService.activateSubscriptionAdmin(id);

        assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    @DisplayName("activateSubscriptionAdmin throws when subscription is not PENDING_PAYMENT")
    void activateSubscriptionAdmin_Throws_WhenNotPendingPayment() {
        UUID id = UUID.randomUUID();
        Subscription sub = Subscription.builder()
            .id(id).status(SubscriptionStatus.EXPIRED).build();

        when(subscriptionRepository.findById(id)).thenReturn(Optional.of(sub));

        assertThatThrownBy(() -> subscriptionService.activateSubscriptionAdmin(id))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("EXPIRED");
    }
}