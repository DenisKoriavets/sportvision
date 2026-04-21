package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.request.BulkAttendanceRequest;

public interface AttendanceService {
    void markBulkAttendance(BulkAttendanceRequest request);
}
