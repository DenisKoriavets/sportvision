package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.SessionGenerationRequest;
import com.github.deniskoriavets.sportvision.dto.SessionRequest;
import com.github.deniskoriavets.sportvision.dto.SessionResponse;
import com.github.deniskoriavets.sportvision.dto.SessionSearchCriteria;
import com.github.deniskoriavets.sportvision.entity.Session;
import com.github.deniskoriavets.sportvision.entity.enums.SessionStatus;
import com.github.deniskoriavets.sportvision.mapper.SessionMapper;
import com.github.deniskoriavets.sportvision.repository.GroupRepository;
import com.github.deniskoriavets.sportvision.repository.ScheduleRepository;
import com.github.deniskoriavets.sportvision.repository.SessionRepository;
import com.github.deniskoriavets.sportvision.repository.specification.SessionSpecifications;
import com.github.deniskoriavets.sportvision.service.interfaces.SessionService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final ScheduleRepository scheduleRepository;
    private final GroupRepository groupRepository;
    private final SessionMapper sessionMapper;

    @Override
    @Transactional
    public void generateSessions(SessionGenerationRequest request) {
        var schedules = scheduleRepository.findAllByGroupId(request.groupId());
        if (schedules.isEmpty()) {
            return;
        }

        List<Session> sessionsToSave = new ArrayList<>();

        request.startDate().datesUntil(request.endDate().plusDays(1)).forEach(date -> {
            var dayOfWeekName = date.getDayOfWeek().name();

            schedules.stream()
                .filter(s -> s.getDayOfWeek().name().equals(dayOfWeekName))
                .forEach(schedule -> {
                    if (!sessionRepository.existsByGroupIdAndDateAndStartTime(
                        request.groupId(), date, schedule.getStartTime())) {

                        var session = Session.builder()
                            .group(schedule.getGroup())
                            .schedule(schedule)
                            .date(date)
                            .startTime(schedule.getStartTime())
                            .endTime(schedule.getEndTime())
                            .status(SessionStatus.SCHEDULED)
                            .build();

                        sessionsToSave.add(session);
                    }
                });
        });

        if (!sessionsToSave.isEmpty()) {
            sessionRepository.saveAll(sessionsToSave);
        }
    }

    @Override
    @Transactional
    public SessionResponse createManualSession(SessionRequest request) {
        var session = Session.builder()
            .group(groupRepository.findById(request.groupId())
                .orElseThrow(() -> new IllegalArgumentException("Group not found")))
            .date(request.date())
            .startTime(request.startTime())
            .endTime(request.endTime())
            .status(SessionStatus.SCHEDULED)
            .build();
        return sessionMapper.toResponse(sessionRepository.save(session));
    }

    @Override
    @Transactional
    public void cancelSession(UUID sessionId, String reason) {
        var session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        session.setStatus(SessionStatus.CANCELLED);
        session.setCancelReason(reason);
        sessionRepository.save(session);
    }

    @Override
    public List<SessionResponse> getSessionsByGroup(UUID groupId, LocalDate start, LocalDate end) {
        var sessions = sessionRepository.findAllByGroupIdAndDateBetween(groupId, start, end);
        return sessions.stream()
            .map(sessionMapper::toResponse)
            .toList();
    }

    @Override
    public Page<SessionResponse> searchSessions(SessionSearchCriteria criteria, Pageable pageable) {
        var spec = SessionSpecifications.build(criteria);
        return sessionRepository.findAll(spec, pageable)
            .map(sessionMapper::toResponse);
    }
}
