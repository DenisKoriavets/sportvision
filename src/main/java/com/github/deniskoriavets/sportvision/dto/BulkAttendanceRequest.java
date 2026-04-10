package com.github.deniskoriavets.sportvision.dto;

import java.util.List;
import java.util.UUID;

public record BulkAttendanceRequest(
    UUID sessionId,
    List<ChildAttendanceDto> attendances
) {}