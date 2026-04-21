package com.github.deniskoriavets.sportvision.dto.request;

import com.github.deniskoriavets.sportvision.entity.enums.AttendanceStatus;
import java.util.UUID;

public record AttendanceRequest(
    UUID sessionId,
    UUID childId,
    AttendanceStatus status
) {
}
