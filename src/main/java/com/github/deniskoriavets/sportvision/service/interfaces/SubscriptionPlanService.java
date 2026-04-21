package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.request.SubscriptionPlanRequest;
import com.github.deniskoriavets.sportvision.dto.response.SubscriptionPlanResponse;
import com.github.deniskoriavets.sportvision.dto.criteria.SubscriptionPlanSearchCriteria;
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
