package com.github.deniskoriavets.sportvision.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PaymentRequest(
    @NotNull
    UUID subscriptionPlanId,
    @NotNull
    UUID childId
) {
}
