package com.github.deniskoriavets.sportvision.event;

import java.util.UUID;

public record PaymentExpiredEvent(
    UUID paymentId,
    UUID childId,
    UUID parentId
) {}