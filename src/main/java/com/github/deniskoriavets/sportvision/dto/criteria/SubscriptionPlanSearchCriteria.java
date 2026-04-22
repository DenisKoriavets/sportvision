package com.github.deniskoriavets.sportvision.dto.criteria;

import java.math.BigDecimal;
import java.util.UUID;

public record SubscriptionPlanSearchCriteria(
    UUID sectionId,
    String query,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Boolean isUnlimited,
    Integer validityDays,
    Boolean isActive
) {
}
