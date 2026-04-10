package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.SubscriptionPlanRequest;
import com.github.deniskoriavets.sportvision.dto.SubscriptionPlanResponse;
import com.github.deniskoriavets.sportvision.dto.SubscriptionPlanSearchCriteria;
import com.github.deniskoriavets.sportvision.exception.ResourceNotFoundException;
import com.github.deniskoriavets.sportvision.mapper.SubscriptionPlanMapper;
import com.github.deniskoriavets.sportvision.repository.SectionRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionPlanRepository;
import com.github.deniskoriavets.sportvision.repository.specification.SubscriptionPlanSpecifications;
import com.github.deniskoriavets.sportvision.service.interfaces.SubscriptionPlanService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;
    private final SectionRepository sectionRepository;

    @Override
    public SubscriptionPlanResponse createSubscriptionPlan(
        SubscriptionPlanRequest subscriptionPlanRequest) {
        var session = sectionRepository.findById(subscriptionPlanRequest.sectionId())
            .orElseThrow(() -> new ResourceNotFoundException("Section not found"));
        var subscriptionPlan = subscriptionPlanMapper.toEntity(subscriptionPlanRequest);
        subscriptionPlan.setSection(session);
        return subscriptionPlanMapper.toResponse(subscriptionPlanRepository.save(subscriptionPlan));
    }

    @Override
    public SubscriptionPlanResponse getSubscriptionPlanById(UUID id) {
        return subscriptionPlanRepository.findById(id)
            .map(subscriptionPlanMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found"));
    }

    @Override
    public Page<SubscriptionPlanResponse> getAllSubscriptionPlans(
        SubscriptionPlanSearchCriteria searchCriteria, Pageable pageable) {
        return subscriptionPlanRepository.findAll(
                SubscriptionPlanSpecifications.build(searchCriteria), pageable)
            .map(subscriptionPlanMapper::toResponse);
    }

    @Override
    public SubscriptionPlanResponse updateSubscriptionPlan(UUID id,
                                                           SubscriptionPlanRequest request) {
        var subscriptionPlan = subscriptionPlanRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found"));
        var session = sectionRepository.findById(request.sectionId())
            .orElseThrow(() -> new ResourceNotFoundException("Section not found"));
        subscriptionPlan.setSection(session);
        subscriptionPlan.setName(request.name());
        subscriptionPlan.setPrice(request.price());
        subscriptionPlan.setSessionsCount(request.sessionsCount());
        subscriptionPlan.setValidityDays(request.validityDays());
        subscriptionPlan.setUnlimited(request.isUnlimited());
        return subscriptionPlanMapper.toResponse(subscriptionPlanRepository.save(subscriptionPlan));
    }

    @Override
    public void deleteSubscriptionPlan(UUID id) {
        subscriptionPlanRepository.deleteById(id);
    }
}
