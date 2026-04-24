package com.github.deniskoriavets.sportvision.event;

import java.util.UUID;

public record SubscriptionExpiredEvent(
    UUID subscriptionId,
    UUID childId,
    UUID parentId
) {
}
