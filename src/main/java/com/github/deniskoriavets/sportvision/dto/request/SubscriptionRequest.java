package com.github.deniskoriavets.sportvision.dto.request;

import java.util.UUID;

public record SubscriptionRequest(
    UUID childId,
    UUID planId
) {
}
