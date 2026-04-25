package com.github.deniskoriavets.sportvision.event;

import java.util.UUID;

public record PaymentSuccessEvent(
    UUID paymentId,
    int amount,
    UUID subscriptionPlanId,
    UUID childId
) {
}
