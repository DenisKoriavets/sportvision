package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.request.SessionGenerationRequest;
import com.github.deniskoriavets.sportvision.entity.Group;
import com.github.deniskoriavets.sportvision.entity.Schedule;
import com.github.deniskoriavets.sportvision.entity.enums.DayOfWeek;
import com.github.deniskoriavets.sportvision.repository.ScheduleRepository;
import com.github.deniskoriavets.sportvision.repository.SessionRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private SessionServiceImpl sessionService;

    @Test
    @DisplayName("Успішна генерація занять для валідного періоду")
    void generateSessions_Success() {
        UUID groupId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now().with(TemporalAdjusters.next(java.time.DayOfWeek.MONDAY));
        LocalDate endDate = startDate.plusDays(7);
        SessionGenerationRequest request = new SessionGenerationRequest(groupId, startDate, endDate);

        Schedule schedule = new Schedule();
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(10, 0));
        schedule.setEndTime(LocalTime.of(11, 0));
        schedule.setGroup(new Group());

        when(scheduleRepository.findAllByGroupId(groupId)).thenReturn(List.of(schedule));
        when(sessionRepository.existsByGroupIdAndDateAndStartTime(eq(groupId), any(LocalDate.class), any(LocalTime.class)))
            .thenReturn(false);

        sessionService.generateSessions(request);

        verify(sessionRepository).saveAll(argThat(it -> it != null && it.iterator().hasNext()));
    }

    @Test
    @DisplayName("Викидання помилки при некоректному діапазоні дат")
    void generateSessions_InvalidDates() {
        UUID scheduleId = UUID.randomUUID();
        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(5);
        SessionGenerationRequest request = new SessionGenerationRequest(scheduleId, startDate, endDate);

        assertThrows(IllegalArgumentException.class, () -> sessionService.generateSessions(request));
    }
}