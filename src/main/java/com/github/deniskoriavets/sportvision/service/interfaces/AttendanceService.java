package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.request.BulkAttendanceRequest;
import com.github.deniskoriavets.sportvision.dto.response.AttendanceResponse;
import com.github.deniskoriavets.sportvision.entity.enums.AttendanceStatus;
import java.util.UUID;

public interface AttendanceService {
    void markBulkAttendance(BulkAttendanceRequest request);

    AttendanceResponse updateAttendanceStatus(UUID sessionId, UUID childId, AttendanceStatus newStatus);
}
