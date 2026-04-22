package com.github.deniskoriavets.sportvision.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record SubscriptionPlanResponse(
    UUID id,
    String name,
    BigDecimal price,
    Integer sessionCount,
    UUID sectionId,
    String sectionName,
    Integer validityDays,
    Boolean isUnlimited
) {
}
