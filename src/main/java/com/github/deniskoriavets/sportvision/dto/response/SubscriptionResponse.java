package com.github.deniskoriavets.sportvision.dto.response;

import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import java.time.LocalDate;
import java.util.UUID;

public record SubscriptionResponse(
    UUID id,
    UUID childId,
    UUID planId,
    String planName,
    LocalDate startDate,
    LocalDate endDate,
    Integer remainingSessions,
    SubscriptionStatus status
) {
}
