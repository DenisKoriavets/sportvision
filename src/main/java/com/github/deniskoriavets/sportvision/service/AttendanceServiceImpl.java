package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.request.BulkAttendanceRequest;
import com.github.deniskoriavets.sportvision.dto.response.AttendanceResponse;
import com.github.deniskoriavets.sportvision.entity.enums.AttendanceStatus;
import com.github.deniskoriavets.sportvision.entity.enums.SessionStatus;
import com.github.deniskoriavets.sportvision.mapper.AttendanceMapper;
import com.github.deniskoriavets.sportvision.entity.Attendance;
import com.github.deniskoriavets.sportvision.event.AttendanceMarkedEvent;
import com.github.deniskoriavets.sportvision.repository.AttendanceRepository;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.repository.SessionRepository;
import com.github.deniskoriavets.sportvision.security.SecurityFacade;
import com.github.deniskoriavets.sportvision.service.interfaces.AttendanceService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SecurityFacade securityFacade;
    private final ChildRepository childRepository;
    private final SessionRepository sessionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AttendanceMapper attendanceMapper;

    @Override
    @Transactional
    public void markBulkAttendance(BulkAttendanceRequest request) {
        var currentUser = securityFacade.getCurrentUser();
        var session = sessionRepository.findById(request.sessionId())
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        for (var attendance : request.attendances()) {
            if (attendanceRepository.existsBySessionIdAndChildId(session.getId(), attendance.childId())) {
                continue;
            }

            var child = childRepository.findById(attendance.childId())
                .orElseThrow(() -> new IllegalArgumentException("Child not found"));

            var attendanceEntity = Attendance.builder()
                .child(child)
                .session(session)
                .markedAt(LocalDateTime.now())
                .markedByCoach(currentUser)
                .status(attendance.status())
                .build();
            attendanceRepository.save(attendanceEntity);
            eventPublisher.publishEvent(
                new AttendanceMarkedEvent(child.getId(), session.getId(), attendance.status()));
        }
    }

    @Override
    @Transactional
    public AttendanceResponse updateAttendanceStatus(UUID sessionId, UUID childId, AttendanceStatus newStatus) {
        var session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new IllegalStateException(
                "Cannot update attendance for a session with status: " + session.getStatus());
        }

        var attendance = attendanceRepository.findBySessionIdAndChildId(sessionId, childId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Attendance record not found for this session and child"));

        attendance.setStatus(newStatus);
        return attendanceMapper.toResponse(attendanceRepository.save(attendance));
    }
}
