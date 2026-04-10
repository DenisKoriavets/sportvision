package com.github.deniskoriavets.sportvision.dto;

import java.util.UUID;

public record SubscriptionRequest(
    UUID childId,
    UUID planId
) {
}
