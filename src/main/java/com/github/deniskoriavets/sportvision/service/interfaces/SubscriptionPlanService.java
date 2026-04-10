package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.SubscriptionPlanRequest;
import com.github.deniskoriavets.sportvision.dto.SubscriptionPlanResponse;
import com.github.deniskoriavets.sportvision.dto.SubscriptionPlanSearchCriteria;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubscriptionPlanService {
    SubscriptionPlanResponse createSubscriptionPlan(
        SubscriptionPlanRequest subscriptionPlanRequest);

    SubscriptionPlanResponse getSubscriptionPlanById(UUID id);

    Page<SubscriptionPlanResponse> getAllSubscriptionPlans(
        SubscriptionPlanSearchCriteria searchCriteria, Pageable pageable);

    SubscriptionPlanResponse updateSubscriptionPlan(UUID id, SubscriptionPlanRequest request);

    void deleteSubscriptionPlan(UUID id);
}
