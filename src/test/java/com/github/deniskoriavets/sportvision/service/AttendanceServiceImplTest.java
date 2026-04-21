package com.github.deniskoriavets.sportvision.service;

import static org.mockito.Mockito.*;

import com.github.deniskoriavets.sportvision.dto.BulkAttendanceRequest;
import com.github.deniskoriavets.sportvision.dto.ChildAttendanceDto;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.Session;
import com.github.deniskoriavets.sportvision.entity.enums.AttendanceStatus;
import com.github.deniskoriavets.sportvision.repository.AttendanceRepository;
import com.github.deniskoriavets.sportvision.repository.SessionRepository;
import com.github.deniskoriavets.sportvision.security.SecurityFacade;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private SessionRepository sessionRepository;
    @Mock private SecurityFacade securityFacade;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private AttendanceServiceImpl attendanceService;

    @Test
    void markBulkAttendance_ShouldNotSaveDuplicate_WhenAlreadyExists() {
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