package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.SubscriptionRequest;
import com.github.deniskoriavets.sportvision.dto.SubscriptionResponse;
import java.util.List;
import java.util.UUID;

public interface SubscriptionService {
    SubscriptionResponse buySubscription(SubscriptionRequest subscriptionRequest);

    List<SubscriptionResponse> getChildSubscriptions(UUID childId);

    void cancelSubscription(UUID id);

    SubscriptionResponse getSubscriptionById(UUID id);
}
