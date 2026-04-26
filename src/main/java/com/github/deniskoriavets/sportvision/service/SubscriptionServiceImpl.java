package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.request.PaymentRequest;
import com.github.deniskoriavets.sportvision.dto.request.SubscriptionRequest;
import com.github.deniskoriavets.sportvision.dto.response.PaymentResponse;
import com.github.deniskoriavets.sportvision.dto.response.SubscriptionResponse;
import com.github.deniskoriavets.sportvision.entity.Child;
import com.github.deniskoriavets.sportvision.entity.Payment;
import com.github.deniskoriavets.sportvision.entity.Subscription;
import com.github.deniskoriavets.sportvision.entity.enums.PaymentStatus;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import com.github.deniskoriavets.sportvision.event.PaymentSuccessEvent;
import com.github.deniskoriavets.sportvision.exception.ResourceNotFoundException;
import com.github.deniskoriavets.sportvision.mapper.SubscriptionMapper;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.repository.PaymentRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionPlanRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionRepository;
import com.github.deniskoriavets.sportvision.security.SecurityFacade;
import com.github.deniskoriavets.sportvision.service.interfaces.PaymentGateway;
import com.github.deniskoriavets.sportvision.service.interfaces.SubscriptionService;
import com.stripe.exception.StripeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final ChildRepository childRepository;
    private final SecurityFacade securityFacade;
    private final PaymentGateway paymentGateway;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public SubscriptionResponse buySubscription(SubscriptionRequest subscriptionRequest) {
        var child = getChildIfOwner(subscriptionRequest.childId());
        var plan = subscriptionPlanRepository.findById(subscriptionRequest.planId())
            .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found"));

        if (!plan.isActive()) {
            throw new IllegalStateException("Subscription plan is not active");
        }

        if (subscriptionRepository.existsByChildIdAndSubscriptionPlanSectionIdAndStatusIn(
            child.getId(), plan.getSection().getId(), List.of(
                SubscriptionStatus.ACTIVE, SubscriptionStatus.PENDING_PAYMENT))) {
            throw new IllegalStateException(
                "Child already has an active or pending subscription for this section");
        }

        var subscription = Subscription.builder()
            .child(child)
            .subscriptionPlan(plan)
            .totalSessions(plan.getSessionsCount())
            .remainingSessions(plan.getSessionsCount())
            .status(SubscriptionStatus.PENDING_PAYMENT)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusDays(plan.getValidityDays()))
            .build();

        var savedSubscription = subscriptionRepository.save(subscription);
        return subscriptionMapper.toResponse(savedSubscription);
    }

    @Override
    public List<SubscriptionResponse> getChildSubscriptions(UUID childId) {
        var child = getChildIfOwner(childId);
        return subscriptionRepository.findAllByChildId(child.getId()).stream()
            .map(subscriptionMapper::toResponse)
            .toList();
    }

    @Override
    public void cancelSubscription(UUID id) {
        var subscription = subscriptionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        if (!subscription.getChild().getParent().getId()
            .equals(securityFacade.getCurrentUserId())) {
            throw new AccessDeniedException("Access to this subscription is denied");
        }

        if (subscription.getStatus() == SubscriptionStatus.ACTIVE ||
            subscription.getStatus() == SubscriptionStatus.PENDING_PAYMENT) {
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscriptionRepository.save(subscription);
        } else {
            throw new IllegalStateException("Only active subscriptions can be canceled");
        }
    }

    @Override
    public SubscriptionResponse getSubscriptionById(UUID id) {
        var subscription = subscriptionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));

        if (!subscription.getChild().getParent().getId()
            .equals(securityFacade.getCurrentUserId())) {
            throw new AccessDeniedException("Access to this subscription is denied");
        }

        return subscriptionMapper.toResponse(subscription);
    }

    @Override
    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request)
        throws StripeException {
        var child = getChildIfOwner(request.childId());
        var subscriptionPlan = subscriptionPlanRepository.findById(request.subscriptionPlanId())
            .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found"));

        var payment = Payment.builder()
            .amount(subscriptionPlan.getPrice())
            .createdAt(LocalDateTime.now())
            .status(PaymentStatus.PENDING)
            .build();
        paymentRepository.save(payment);
        var metadata = Map.of("payment_id", payment.getId().toString(), "plan_id",
            subscriptionPlan.getId().toString(), "child_id", child.getId().toString());
        return paymentGateway.createPaymentSession(payment.getAmount().longValue() * 100, "UAH",
            subscriptionPlan.getName(), metadata);
    }

    @Override
    @Transactional
    public void completePayment(UUID paymentId) {
        var payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        if (payment.getStatus() == PaymentStatus.PAID) {
            log.info("Payment {} already processed. Skipping.", paymentId);
            return;
        }

        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);

        var subscription = payment.getSubscription();
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(LocalDate.now());
        subscription.setEndDate(
            LocalDate.now().plusDays(subscription.getSubscriptionPlan().getValidityDays()));
        subscriptionRepository.save(subscription);

        eventPublisher.publishEvent(
            new PaymentSuccessEvent(payment.getId(), payment.getAmount().intValue() * 100,
                subscription.getSubscriptionPlan().getId(), subscription.getChild().getId()));
    }

    private Child getChildIfOwner(UUID id) {
        Child child = childRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        if (!child.getParent().getId().equals(securityFacade.getCurrentUserId())) {
            throw new AccessDeniedException("Access to this child's data is denied");
        }
        return child;
    }
}
