package com.github.deniskoriavets.sportvision.mapper;

import com.github.deniskoriavets.sportvision.dto.SubscriptionRequest;
import com.github.deniskoriavets.sportvision.dto.SubscriptionResponse;
import com.github.deniskoriavets.sportvision.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionMapper {

    @Mapping(target = "childId", source = "child.id")
    @Mapping(target = "planId", source = "subscriptionPlan.id")
    @Mapping(target = "planName", source = "subscriptionPlan.name")
    SubscriptionResponse toResponse(Subscription subscription);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "child", ignore = true)
    @Mapping(target = "subscriptionPlan", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Subscription toEntity(SubscriptionRequest subscriptionRequest);
}
