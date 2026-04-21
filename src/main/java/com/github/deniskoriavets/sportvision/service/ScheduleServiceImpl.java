package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.request.ScheduleRequest;
import com.github.deniskoriavets.sportvision.dto.response.ScheduleResponse;
import com.github.deniskoriavets.sportvision.exception.ResourceNotFoundException;
import com.github.deniskoriavets.sportvision.mapper.ScheduleMapper;
import com.github.deniskoriavets.sportvision.repository.GroupRepository;
import com.github.deniskoriavets.sportvision.repository.ScheduleRepository;
import com.github.deniskoriavets.sportvision.service.interfaces.ScheduleService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final GroupRepository groupRepository;
    private final ScheduleMapper scheduleMapper;

    @Override
    public ScheduleResponse createSchedule(ScheduleRequest scheduleRequest) {
        if (scheduleRequest.endTime().isBefore(scheduleRequest.startTime())) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
        var group = groupRepository.findById(scheduleRequest.groupId()).orElseThrow(() ->
            new ResourceNotFoundException("Group not found"));
        if (scheduleRepository.existsCoachConflict(group.getCoach().getId(),
            scheduleRequest.dayOfWeek(), scheduleRequest.startTime(), scheduleRequest.endTime())) {
            throw new IllegalStateException("Coach has a scheduling conflict");
        }
        var schedule = scheduleMapper.toEntity(scheduleRequest);
        schedule.setGroup(group);
        return scheduleMapper.toResponse(scheduleRepository.save(schedule));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getGroupSchedules(UUID groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group not found");
        }
        return scheduleRepository.findAllByGroupId(groupId).stream()
            .map(scheduleMapper::toResponse)
            .toList();
    }

    @Override
    public void deleteSchedule(UUID scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }
}
