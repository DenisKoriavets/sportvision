package com.github.deniskoriavets.sportvision.dto.request;

import com.github.deniskoriavets.sportvision.entity.enums.DayOfWeek;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduleRequest(
    @NotNull(message = "Group ID must not be null") UUID groupId,
    @NotNull(message = "Day of week must not be null") DayOfWeek dayOfWeek,
    @NotNull(message = "Start time must not be null") LocalTime startTime,
    @NotNull(message = "End time must not be null") LocalTime endTime,
    String location
) {
}
