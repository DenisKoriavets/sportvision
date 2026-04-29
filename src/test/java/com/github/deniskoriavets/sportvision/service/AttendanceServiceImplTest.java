package com.github.deniskoriavets.sportvision.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.deniskoriavets.sportvision.dto.ChildAttendanceDto;
import com.github.deniskoriavets.sportvision.dto.request.BulkAttendanceRequest;
import com.github.deniskoriavets.sportvision.dto.response.AttendanceResponse;
import com.github.deniskoriavets.sportvision.entity.Attendance;
import com.github.deniskoriavets.sportvision.entity.Child;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.Session;
import com.github.deniskoriavets.sportvision.entity.enums.AttendanceStatus;
import com.github.deniskoriavets.sportvision.entity.enums.SessionStatus;
import com.github.deniskoriavets.sportvision.event.AttendanceMarkedEvent;
import com.github.deniskoriavets.sportvision.mapper.AttendanceMapper;
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
    @Mock private AttendanceMapper attendanceMapper;

    @InjectMocks private AttendanceServiceImpl attendanceService;

    @Test
    @DisplayName("Saves attendance and publishes event when status is PRESENT")
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
    @DisplayName("Saves attendance and publishes event when status is ABSENT")
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
    @DisplayName("Skips duplicate attendance — save and publishEvent are not called")
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

    @Test
    @DisplayName("updateAttendanceStatus updates status when session is SCHEDULED")
    void updateAttendanceStatus_UpdatesStatus_WhenSessionScheduled() {
        UUID sessionId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();

        Session session = Session.builder().id(sessionId).status(SessionStatus.SCHEDULED).build();
        Child child = Child.builder().id(childId).build();
        Attendance attendance = Attendance.builder()
            .session(session)
            .child(child)
            .status(AttendanceStatus.ABSENT)
            .markedByCoach(Parent.builder().id(UUID.randomUUID()).build())
            .build();
        Attendance updated = Attendance.builder()
            .session(session)
            .child(child)
            .status(AttendanceStatus.PRESENT)
            .markedByCoach(Parent.builder().id(UUID.randomUUID()).build())
            .build();

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(attendanceRepository.findBySessionIdAndChildId(sessionId, childId))
            .thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(attendance)).thenReturn(updated);
        when(attendanceMapper.toResponse(updated)).thenReturn(mock(AttendanceResponse.class));

        attendanceService.updateAttendanceStatus(sessionId, childId, AttendanceStatus.PRESENT);

        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        verify(attendanceRepository).save(attendance);
    }

    @Test
    @DisplayName("updateAttendanceStatus throws when session is COMPLETED")
    void updateAttendanceStatus_Throws_WhenSessionCompleted() {
        UUID sessionId = UUID.randomUUID();
        Session session = Session.builder().id(sessionId).status(SessionStatus.COMPLETED).build();

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() ->
            attendanceService.updateAttendanceStatus(sessionId, UUID.randomUUID(), AttendanceStatus.PRESENT))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("COMPLETED");
    }
}
