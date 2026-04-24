package com.github.deniskoriavets.sportvision.event;

import java.util.UUID;

public record SessionReminderEvent(
    UUID sessionId,
    UUID groupId
) {
}
