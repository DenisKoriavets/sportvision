package com.github.deniskoriavets.sportvision.scheduler;

import com.github.deniskoriavets.sportvision.entity.Child;
import com.github.deniskoriavets.sportvision.entity.Group;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.Session;
import com.github.deniskoriavets.sportvision.entity.Subscription;
import com.github.deniskoriavets.sportvision.entity.enums.SessionStatus;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import com.github.deniskoriavets.sportvision.event.SessionReminderEvent;
import com.github.deniskoriavets.sportvision.event.SubscriptionLowEvent;
import com.github.deniskoriavets.sportvision.repository.SessionRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private NotificationScheduler notificationScheduler;

    @Test
    void scheduleSessionReminders_ShouldPublishEvents() {
        Session mockSession = new Session();
        mockSession.setId(UUID.randomUUID());
        Group mockGroup = new Group();
        mockGroup.setId(UUID.randomUUID());
        mockSession.setGroup(mockGroup);

        when(sessionRepository.findAllByDateAndStatus(any(LocalDate.class), eq(SessionStatus.SCHEDULED)))
            .thenReturn(List.of(mockSession));

        notificationScheduler.scheduleSessionReminders();

        verify(eventPublisher, times(1)).publishEvent(any(SessionReminderEvent.class));
    }

    @Test
    void scheduleSubscriptionExpiryReminders_ShouldPublishEvents() {
        Subscription mockSubscription = new Subscription();
        mockSubscription.setId(UUID.randomUUID());
        mockSubscription.setRemainingSessions(1);
        
        Child mockChild = new Child();
        Parent mockParent = new Parent();
        mockParent.setId(UUID.randomUUID());
        mockChild.setParent(mockParent);
        mockSubscription.setChild(mockChild);

        when(subscriptionRepository.findAllByStatusAndRemainingSessionsLessThan(
            eq(SubscriptionStatus.ACTIVE), eq(2)))
            .thenReturn(List.of(mockSubscription));

        notificationScheduler.scheduleSubscriptionExpiryReminders();

        verify(eventPublisher, times(1)).publishEvent(any(SubscriptionLowEvent.class));
    }
}