package com.github.deniskoriavets.sportvision.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.deniskoriavets.sportvision.dto.ChildAttendanceDto;
import com.github.deniskoriavets.sportvision.dto.request.BulkAttendanceRequest;
import com.github.deniskoriavets.sportvision.entity.Child;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.Session;
import com.github.deniskoriavets.sportvision.entity.enums.AttendanceStatus;
import com.github.deniskoriavets.sportvision.event.AttendanceMarkedEvent;
import com.github.deniskoriavets.sportvision.repository.AttendanceRepository;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.repository.SessionRepository;
import com.github.deniskoriavets.sportvision.security.SecurityFacade;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private ChildRepository childRepository;
    @Mock private SessionRepository sessionRepository;
    @Mock private SecurityFacade securityFacade;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private AttendanceServiceImpl attendanceService;

    @Test
    @DisplayName("Успішне збереження відмітки і публікація події при статусі PRESENT")
    void markBulkAttendance_SavesAttendanceAndPublishesEvent() {
        UUID sessionId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();
        BulkAttendanceRequest request = new BulkAttendanceRequest(sessionId,
            List.of(new ChildAttendanceDto(childId, AttendanceStatus.PRESENT)));

        Session session = new Session();
        session.setId(sessionId);
        Child child = new Child();
        child.setId(childId);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(securityFacade.getCurrentUser()).thenReturn(new Parent());
        when(attendanceRepository.existsBySessionIdAndChildId(sessionId, childId)).thenReturn(false);
        when(childRepository.findById(childId)).thenReturn(Optional.of(child));

        attendanceService.markBulkAttendance(request);

        verify(attendanceRepository).save(any());
        verify(eventPublisher).publishEvent(any(AttendanceMarkedEvent.class));
    }

    @Test
    @DisplayName("Подія не публікується при статусі ABSENT")
    void markBulkAttendance_DoesNotPublishEvent_WhenAbsent() {
        UUID sessionId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();
        BulkAttendanceRequest request = new BulkAttendanceRequest(sessionId,
            List.of(new ChildAttendanceDto(childId, AttendanceStatus.ABSENT)));

        Session session = new Session();
        session.setId(sessionId);
        Child child = new Child();
        child.setId(childId);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(securityFacade.getCurrentUser()).thenReturn(new Parent());
        when(attendanceRepository.existsBySessionIdAndChildId(sessionId, childId)).thenReturn(false);
        when(childRepository.findById(childId)).thenReturn(Optional.of(child));

        attendanceService.markBulkAttendance(request);

        verify(attendanceRepository).save(any());
        verify(eventPublisher).publishEvent(any(AttendanceMarkedEvent.class));
    }

    @Test
    @DisplayName("Дублікат відмітки — save і publishEvent не викликаються")
    void markBulkAttendance_SkipsDuplicate() {
        UUID sessionId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();
        BulkAttendanceRequest request = new BulkAttendanceRequest(sessionId,
            List.of(new ChildAttendanceDto(childId, AttendanceStatus.PRESENT)));

        Session session = new Session();
        session.setId(sessionId);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(securityFacade.getCurrentUser()).thenReturn(new Parent());
        when(attendanceRepository.existsBySessionIdAndChildId(sessionId, childId)).thenReturn(true);

        attendanceService.markBulkAttendance(request);

        verify(attendanceRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
