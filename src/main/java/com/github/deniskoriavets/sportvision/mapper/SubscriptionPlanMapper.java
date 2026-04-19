package com.github.deniskoriavets.sportvision.mapper;

import com.github.deniskoriavets.sportvision.dto.SubscriptionPlanRequest;
import com.github.deniskoriavets.sportvision.dto.SubscriptionPlanResponse;
import com.github.deniskoriavets.sportvision.entity.SubscriptionPlan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionPlanMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    SubscriptionPlan toEntity(SubscriptionPlanRequest subscriptionPlanRequest);

    @Mapping(source = "section.name", target = "sectionName")
    @Mapping(source = "section.id", target = "sectionId")
    @Mapping(target = "sessionCount", source = "sessionsCount")
    SubscriptionPlanResponse toResponse(SubscriptionPlan subscriptionPlan);
}
