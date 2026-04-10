package com.github.deniskoriavets.sportvision.mapper;

import com.github.deniskoriavets.sportvision.dto.SubscriptionRequest;
import com.github.deniskoriavets.sportvision.dto.SubscriptionResponse;
import com.github.deniskoriavets.sportvision.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    @Mapping(source = "subscriptionPlan.name", target = "planName")
    SubscriptionResponse toResponse(Subscription subscription);

    Subscription toEntity(SubscriptionRequest subscriptionRequest);
}
