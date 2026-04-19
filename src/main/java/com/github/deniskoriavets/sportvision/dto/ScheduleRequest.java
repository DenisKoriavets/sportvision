package com.github.deniskoriavets.sportvision.dto;

import com.github.deniskoriavets.sportvision.entity.enums.DayOfWeek;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduleRequest(
    @NotNull UUID groupId,
    @NotNull DayOfWeek dayOfWeek,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    String location
) {
}
