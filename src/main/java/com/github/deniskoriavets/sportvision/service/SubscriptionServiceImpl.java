package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.SubscriptionRequest;
import com.github.deniskoriavets.sportvision.dto.SubscriptionResponse;
import com.github.deniskoriavets.sportvision.entity.Child;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import com.github.deniskoriavets.sportvision.exception.ResourceNotFoundException;
import com.github.deniskoriavets.sportvision.mapper.SubscriptionMapper;
import com.github.deniskoriavets.sportvision.mapper.SubscriptionPlanMapper;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionPlanRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionRepository;
import com.github.deniskoriavets.sportvision.security.SecurityFacade;
import com.github.deniskoriavets.sportvision.service.interfaces.SubscriptionService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final ChildRepository childRepository;
    private final SecurityFacade securityFacade;

    @Override
    @Transactional
    public SubscriptionResponse buySubscription(SubscriptionRequest subscriptionRequest) {
        var child = getChildIfOwner(subscriptionRequest.childId());
        var plan = subscriptionPlanRepository.findById(subscriptionRequest.planId())
            .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found"));

        if (plan.isActive() &&
            !subscriptionRepository.existsByChildIdAndSubscriptionPlanSectionIdAndStatusIn(
                child.getId(), plan.getSection().getId(), List.of(
                    SubscriptionStatus.ACTIVE, SubscriptionStatus.PENDING_PAYMENT))) {
            var subscription = subscriptionMapper.toEntity(subscriptionRequest);
            subscription.setStatus(SubscriptionStatus.PENDING_PAYMENT);
            subscription.setStartDate(LocalDate.now());
            subscription.setEndDate(LocalDate.now().plusDays(plan.getValidityDays()));
            subscription.setTotalSessions(plan.getSessionsCount());
            subscription.setRemainingSessions(plan.getSessionsCount());
            subscription.setChild(child);
            subscription.setSubscriptionPlan(plan);
            return subscriptionMapper.toResponse(subscriptionRepository.save(subscription));
        }
        if (!plan.isActive()) {
            throw new IllegalStateException("Subscription plan is not active");
        } else {
            throw new IllegalStateException(
                "Child already has an active or pending subscription for this section");
        }
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
            throw new AccessDeniedException("Ви не маєте доступу до даних цієї підписки");
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
            throw new AccessDeniedException("Ви не маєте доступу до даних цієї підписки");
        }

        return subscriptionMapper.toResponse(subscription);
    }

    private Child getChildIfOwner(UUID id) {
        Child child = childRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        if (!child.getParent().getId().equals(securityFacade.getCurrentUserId())) {
            throw new AccessDeniedException("Ви не маєте доступу до даних цієї дитини");
        }
        return child;
    }
}
