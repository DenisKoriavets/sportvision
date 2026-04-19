package com.github.deniskoriavets.sportvision.dto;

import com.github.deniskoriavets.sportvision.entity.enums.SessionStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record SessionResponse(
    UUID id,
    UUID groupId,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    SessionStatus status,
    String cancelReason
) {
}
