package com.github.deniskoriavets.sportvision.dto.request;

import com.github.deniskoriavets.sportvision.validation.ValidSubscriptionPlan;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

@ValidSubscriptionPlan
public record SubscriptionPlanRequest(
    @NotBlank(message = "Plan name must not be blank") String name,
    @PositiveOrZero(message = "Price must be zero or positive") BigDecimal price,
    Integer sessionsCount,
    @NotNull(message = "Section ID must not be null") UUID sectionId,
    @Min(value = 1, message = "Validity days must be at least 1") Integer validityDays,
    Boolean isUnlimited
    ) {
}
