package com.github.deniskoriavets.sportvision.event;

import com.github.deniskoriavets.sportvision.entity.enums.AttendanceStatus;
import java.util.UUID;

public record AttendanceMarkedEvent(
    UUID childId,
    UUID sessionId,
    AttendanceStatus status
) {
}
