package com.github.deniskoriavets.sportvision.dto;

import com.github.deniskoriavets.sportvision.entity.enums.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduleResponse(
    UUID id,
    UUID groupId,
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    String location
) {
}
