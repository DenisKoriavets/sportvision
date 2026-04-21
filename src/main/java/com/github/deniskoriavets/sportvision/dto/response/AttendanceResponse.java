package com.github.deniskoriavets.sportvision.dto.response;

import com.github.deniskoriavets.sportvision.entity.enums.AttendanceStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record AttendanceResponse(
    UUID id,
    UUID sessionId,
    String sessionName,
    AttendanceStatus status,
    LocalDateTime markedAt
) {}