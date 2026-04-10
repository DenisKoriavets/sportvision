package com.github.deniskoriavets.sportvision.dto;

import com.github.deniskoriavets.sportvision.entity.enums.AttendanceStatus;
import java.util.UUID;

public record ChildAttendanceDto(
    UUID childId,
    AttendanceStatus status
) {}