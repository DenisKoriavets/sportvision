package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.request.SessionGenerationRequest;
import com.github.deniskoriavets.sportvision.dto.request.SessionRequest;
import com.github.deniskoriavets.sportvision.dto.response.SessionResponse;
import com.github.deniskoriavets.sportvision.entity.Group;
import com.github.deniskoriavets.sportvision.entity.Schedule;
import com.github.deniskoriavets.sportvision.entity.Session;
import com.github.deniskoriavets.sportvision.entity.enums.DayOfWeek;
import com.github.deniskoriavets.sportvision.entity.enums.SessionStatus;
import com.github.deniskoriavets.sportvision.event.SessionCancelledEvent;
import com.github.deniskoriavets.sportvision.mapper.SessionMapper;
import com.github.deniskoriavets.sportvision.repository.GroupRepository;
import com.github.deniskoriavets.sportvision.repository.ScheduleRepository;
import com.github.deniskoriavets.sportvision.repository.SessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    @Mock private SessionRepository sessionRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private SessionMapper sessionMapper;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private SessionServiceImpl sessionService;

    @Test
    @DisplayName("Generates sessions for matching days from schedule")
    void generateSessions_CreatesSessionsForMatchingDays() {
        UUID groupId = UUID.randomUUID();
        LocalDate start = LocalDate.now().with(TemporalAdjusters.next(java.time.DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(6);

        Group group = new Group();
        Schedule schedule = new Schedule();
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(10, 0));
        schedule.setEndTime(LocalTime.of(11, 0));
        schedule.setGroup(group);

        when(scheduleRepository.findAllByGroupId(groupId)).thenReturn(List.of(schedule));
        when(sessionRepository.existsByGroupIdAndDateAndStartTime(eq(groupId), any(), any())).thenReturn(false);

        sessionService.generateSessions(new SessionGenerationRequest(groupId, start, end));

        verify(sessionRepository).saveAll(argThat(list -> list.iterator().hasNext()));
    }

    @Test
    @DisplayName("Skips sessions that already exist")
    void generateSessions_SkipsExistingSessions() {
        UUID groupId = UUID.randomUUID();
        LocalDate start = LocalDate.now().with(TemporalAdjusters.next(java.time.DayOfWeek.MONDAY));

        Group group = new Group();
        Schedule schedule = new Schedule();
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(10, 0));
        schedule.setEndTime(LocalTime.of(11, 0));
        schedule.setGroup(group);

        when(scheduleRepository.findAllByGroupId(groupId)).thenReturn(List.of(schedule));
        when(sessionRepository.existsByGroupIdAndDateAndStartTime(eq(groupId), any(), any())).thenReturn(true);

        sessionService.generateSessions(new SessionGenerationRequest(groupId, start, start));

        verify(sessionRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Throws exception when start date is in the past")
    void generateSessions_ThrowsException_WhenStartInPast() {
        UUID groupId = UUID.randomUUID();
        LocalDate past = LocalDate.now().minusDays(1);

        assertThrows(IllegalArgumentException.class,
            () -> sessionService.generateSessions(new SessionGenerationRequest(groupId, past, past.plusDays(5))));
    }

    @Test
    @DisplayName("Throws exception when end date is before start date")
    void generateSessions_ThrowsException_WhenEndBeforeStart() {
        UUID groupId = UUID.randomUUID();
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(2);

        assertThrows(IllegalArgumentException.class,
            () -> sessionService.generateSessions(new SessionGenerationRequest(groupId, start, end)));
    }

    @Test
    @DisplayName("Sets session status to CANCELLED")
    void cancelSession_SetsStatusCancelled() {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session();
        session.setId(sessionId);
        session.setStatus(SessionStatus.SCHEDULED);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        sessionService.cancelSession(sessionId, "Тренер захворів");

        assertEquals(SessionStatus.CANCELLED, session.getStatus());
        assertEquals("Тренер захворів", session.getCancelReason());
        verify(sessionRepository).save(session);
        verify(eventPublisher).publishEvent(any(SessionCancelledEvent.class));
    }

    @Test
    @DisplayName("Creates one-off session successfully")
    void createManualSession_Success() {
        UUID groupId = UUID.randomUUID();
        SessionRequest request = new SessionRequest(groupId, LocalDate.now().plusDays(1),
            LocalTime.of(10, 0), LocalTime.of(11, 0));
        Group group = new Group();
        Session saved = new Session();
        SessionResponse response = mock(SessionResponse.class);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(sessionRepository.save(any())).thenReturn(saved);
        when(sessionMapper.toResponse(saved)).thenReturn(response);

        assertNotNull(sessionService.createManualSession(request));
    }
}
