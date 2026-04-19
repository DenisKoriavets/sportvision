package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.ScheduleRequest;
import com.github.deniskoriavets.sportvision.dto.ScheduleResponse;
import java.util.List;
import java.util.UUID;

public interface ScheduleService {
    ScheduleResponse createSchedule(ScheduleRequest scheduleRequest);

    List<ScheduleResponse> getGroupSchedules(UUID groupId);

    void deleteSchedule(UUID scheduleId);
}
