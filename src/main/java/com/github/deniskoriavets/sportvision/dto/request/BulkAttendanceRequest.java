package com.github.deniskoriavets.sportvision.dto.request;

import com.github.deniskoriavets.sportvision.dto.ChildAttendanceDto;
import java.util.List;
import java.util.UUID;

public record BulkAttendanceRequest(
    UUID sessionId,
    List<ChildAttendanceDto> attendances
) {}