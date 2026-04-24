package com.github.deniskoriavets.sportvision.event;

import java.util.UUID;

public record SubscriptionLowEvent(
    UUID subscriptionId,
    UUID parentId,
    int remainingSessions
) {
}
