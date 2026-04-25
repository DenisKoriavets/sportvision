package com.github.deniskoriavets.sportvision.event;

import java.util.UUID;

public record SessionCancelledEvent(
    UUID sessionId,
    String reason
) {
}
