package com.github.deniskoriavets.sportvision.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
