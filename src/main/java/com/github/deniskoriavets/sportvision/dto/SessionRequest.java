package com.github.deniskoriavets.sportvision.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record SessionRequest(
    UUID groupId,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime
) {
}
