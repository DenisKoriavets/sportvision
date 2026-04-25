package com.github.deniskoriavets.sportvision.listener;

import com.github.deniskoriavets.sportvision.entity.Child;
import com.github.deniskoriavets.sportvision.entity.Group;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.Section;
import com.github.deniskoriavets.sportvision.entity.Session;
import com.github.deniskoriavets.sportvision.entity.Subscription;
import com.github.deniskoriavets.sportvision.entity.enums.AttendanceStatus;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import com.github.deniskoriavets.sportvision.event.AttendanceMarkedEvent;
import com.github.deniskoriavets.sportvision.event.SubscriptionExpiredEvent;
import com.github.deniskoriavets.sportvision.event.SubscriptionLowEvent;
import com.github.deniskoriavets.sportvision.repository.SessionRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionDeductionListenerTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SubscriptionDeductionListener listener;

    @Test
    @DisplayName("Should deduct session from active subscription when child is present")
    void onAttendanceMarked_DeductsSession() {
        UUID childId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        AttendanceMarkedEvent event =
            new AttendanceMarkedEvent(childId, sessionId, AttendanceStatus.PRESENT);

        Session session = mock(Session.class);
        com.github.deniskoriavets.sportvision.entity.Group group =
            mock(com.github.deniskoriavets.sportvision.entity.Group.class);
        Section section = mock(Section.class);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(session.getGroup()).thenReturn(group);
        when(group.getSection()).thenReturn(section);
        when(section.getId()).thenReturn(sectionId);

        Subscription subscription = new Subscription();
        subscription.setRemainingSessions(5);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        when(subscriptionRepository.findFirstByChildIdAndSubscriptionPlanSectionIdAndStatus(
            childId, sectionId, SubscriptionStatus.ACTIVE))
            .thenReturn(Optional.of(subscription));

        listener.onAttendanceMarked(event);

        assertEquals(4, subscription.getRemainingSessions());
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    @DisplayName("Should set subscription status to EXPIRED when remaining sessions reach zero")
    void onAttendanceMarked_SetsExpired_WhenZeroRemaining() {
        UUID childId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        AttendanceMarkedEvent event =
            new AttendanceMarkedEvent(childId, sessionId, AttendanceStatus.PRESENT);

        Session session = mock(Session.class);
        com.github.deniskoriavets.sportvision.entity.Group group =
            mock(com.github.deniskoriavets.sportvision.entity.Group.class);
        Section section = mock(Section.class);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(session.getGroup()).thenReturn(group);
        when(group.getSection()).thenReturn(section);
        when(section.getId()).thenReturn(sectionId);

        Subscription subscription = new Subscription();
        subscription.setRemainingSessions(1);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        Parent parent = new Parent();
        parent.setId(UUID.randomUUID());
        Child child = new Child();
        child.setParent(parent);
        subscription.setChild(child);


        when(subscriptionRepository.findFirstByChildIdAndSubscriptionPlanSectionIdAndStatus(
            childId, sectionId, SubscriptionStatus.ACTIVE))
            .thenReturn(Optional.of(subscription));

        listener.onAttendanceMarked(event);

        assertEquals(0, subscription.getRemainingSessions());
        assertEquals(SubscriptionStatus.EXPIRED, subscription.getStatus());
    }

    @Test
    @DisplayName("Should publish SubscriptionExpiredEvent when remaining sessions reach zero")
    void shouldPublishExpiredEventWhenSessionsReachZero() {
        UUID childId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        AttendanceMarkedEvent event =
            new AttendanceMarkedEvent(childId, sessionId, AttendanceStatus.PRESENT);

        Session session = mock(Session.class);
        Group group = mock(Group.class);
        Section section = mock(Section.class);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(session.getGroup()).thenReturn(group);
        when(group.getSection()).thenReturn(section);
        when(section.getId()).thenReturn(sectionId);

        Subscription subscription = new Subscription();
        subscription.setId(UUID.randomUUID());
        subscription.setRemainingSessions(1);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        Parent parent = new Parent();
        parent.setId(UUID.randomUUID());
        Child child = new Child();
        child.setParent(parent);
        subscription.setChild(child);

        when(subscriptionRepository.findFirstByChildIdAndSubscriptionPlanSectionIdAndStatus(
            childId, sectionId, SubscriptionStatus.ACTIVE)).thenReturn(Optional.of(subscription));

        listener.onAttendanceMarked(event);

        ArgumentCaptor<SubscriptionExpiredEvent> eventCaptor =
            ArgumentCaptor.forClass(SubscriptionExpiredEvent.class);

        verify(eventPublisher).publishEvent(eventCaptor.capture());

        SubscriptionExpiredEvent publishedEvent = eventCaptor.getValue();
        assertEquals(subscription.getId(), publishedEvent.subscriptionId());
        assertEquals(parent.getId(), publishedEvent.parentId());
    }

    @Test
    @DisplayName("Should publish SubscriptionLowEvent when remaining sessions are 2 or less")
    void shouldPublishLowEventWhenSessionsAreLow() {
        UUID childId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        AttendanceMarkedEvent event =
            new AttendanceMarkedEvent(childId, sessionId, AttendanceStatus.PRESENT);

        Session session = mock(Session.class);
        Group group = mock(Group.class);
        Section section = mock(Section.class);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(session.getGroup()).thenReturn(group);
        when(group.getSection()).thenReturn(section);
        when(section.getId()).thenReturn(sectionId);

        Subscription subscription = new Subscription();
        subscription.setRemainingSessions(3);

        Parent parent = new Parent();
        parent.setId(UUID.randomUUID());
        Child child = new Child();
        child.setParent(parent);
        subscription.setChild(child);

        when(subscriptionRepository.findFirstByChildIdAndSubscriptionPlanSectionIdAndStatus(
            childId, sectionId, SubscriptionStatus.ACTIVE)).thenReturn(Optional.of(subscription));

        listener.onAttendanceMarked(event);

        ArgumentCaptor<SubscriptionLowEvent> eventCaptor =
            ArgumentCaptor.forClass(SubscriptionLowEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        SubscriptionLowEvent publishedEvent = eventCaptor.getValue();
        assertEquals(2, publishedEvent.remainingSessions());
    }

    @Test
    @DisplayName("Should not publish any events when subscription has enough sessions")
    void shouldNotPublishEventsWhenSessionsAreEnough() {
        UUID childId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        AttendanceMarkedEvent event =
            new AttendanceMarkedEvent(childId, sessionId, AttendanceStatus.PRESENT);

        Session session = mock(Session.class);
        Group group = mock(Group.class);
        Section section = mock(Section.class);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(session.getGroup()).thenReturn(group);
        when(group.getSection()).thenReturn(section);
        when(section.getId()).thenReturn(sectionId);

        Subscription subscription = new Subscription();
        subscription.setRemainingSessions(10);

        Parent parent = new Parent();
        parent.setId(UUID.randomUUID());
        Child child = new Child();
        child.setParent(parent);
        subscription.setChild(child);

        when(subscriptionRepository.findFirstByChildIdAndSubscriptionPlanSectionIdAndStatus(
            childId, sectionId, SubscriptionStatus.ACTIVE)).thenReturn(Optional.of(subscription));

        listener.onAttendanceMarked(event);

        verifyNoInteractions(eventPublisher);
    }
}