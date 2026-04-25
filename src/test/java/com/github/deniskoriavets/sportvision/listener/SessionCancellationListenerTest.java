package com.github.deniskoriavets.sportvision.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.github.deniskoriavets.sportvision.entity.*;
import com.github.deniskoriavets.sportvision.entity.enums.AttendanceStatus;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import com.github.deniskoriavets.sportvision.event.SessionCancelledEvent;
import com.github.deniskoriavets.sportvision.repository.AttendanceRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionCancellationListenerTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SessionCancellationListener listener;

    @Test
    @DisplayName("Should increment session count and reactivate EXPIRED subscription")
    void onSessionCancelled_ShouldCompensateAndReactivate() {
        UUID sessionId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        SessionCancelledEvent event = new SessionCancelledEvent(sessionId, "Coach illness");

        Section section = mock(Section.class);
        when(section.getId()).thenReturn(sectionId);
        
        Group group = mock(Group.class);
        when(group.getSection()).thenReturn(section);
        
        Session session = mock(Session.class);
        when(session.getGroup()).thenReturn(group);

        Child child = mock(Child.class);
        when(child.getId()).thenReturn(childId);

        Attendance attendance = mock(Attendance.class);
        when(attendance.getStatus()).thenReturn(AttendanceStatus.PRESENT);
        when(attendance.getChild()).thenReturn(child);
        when(attendance.getSession()).thenReturn(session);

        Subscription subscription = new Subscription();
        subscription.setRemainingSessions(0);
        subscription.setStatus(SubscriptionStatus.EXPIRED);

        when(attendanceRepository.findAllBySessionId(sessionId)).thenReturn(List.of(attendance));
        when(subscriptionRepository.findFirstByChildIdAndSubscriptionPlanSectionIdAndStatusIn(
            eq(childId),
            eq(sectionId),
            anyCollection()))
            .thenReturn(Optional.of(subscription));

        listener.onSessionCancelled(event);

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());
        
        Subscription savedSub = captor.getValue();
        assertEquals(1, savedSub.getRemainingSessions());
        assertEquals(SubscriptionStatus.ACTIVE, savedSub.getStatus());
    }
}