package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.SubscriptionRequest;
import com.github.deniskoriavets.sportvision.dto.SubscriptionResponse;
import com.github.deniskoriavets.sportvision.entity.Child;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.Section;
import com.github.deniskoriavets.sportvision.entity.Subscription;
import com.github.deniskoriavets.sportvision.entity.SubscriptionPlan;
import com.github.deniskoriavets.sportvision.mapper.SubscriptionMapper;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionPlanRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionRepository;
import com.github.deniskoriavets.sportvision.security.SecurityFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private SubscriptionMapper subscriptionMapper;
    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;
    @Mock
    private ChildRepository childRepository;
    @Mock
    private SecurityFacade securityFacade;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @Test
    @DisplayName("Успішна купівля абонемента при валідних даних")
    void buySubscription_Success() {
        UUID childId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        SubscriptionRequest request = new SubscriptionRequest(childId, planId);

        Parent parent = new Parent();
        parent.setId(parentId);

        Child child = new Child();
        child.setId(childId);
        child.setParent(parent);

        Section section = new Section();
        section.setId(sectionId);

        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setId(planId);
        plan.setActive(true);
        plan.setSection(section);
        plan.setSessionsCount(8);
        plan.setValidityDays(30);

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(securityFacade.getCurrentUserId()).thenReturn(parentId);
        when(subscriptionPlanRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(subscriptionRepository.existsByChildIdAndSubscriptionPlanSectionIdAndStatusIn(any(), any(), any())).thenReturn(false);
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArguments()[0]);
        when(subscriptionMapper.toResponse(any())).thenReturn(mock(SubscriptionResponse.class));

        assertNotNull(subscriptionService.buySubscription(request));
        verify(subscriptionRepository).save(any());
    }

    @Test
    @DisplayName("Помилка при спробі купити неактивний тарифний план")
    void buySubscription_ThrowsException_WhenPlanInactive() {
        UUID childId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        SubscriptionRequest request = new SubscriptionRequest(childId, planId);

        Parent parent = new Parent();
        parent.setId(parentId);
        Child child = new Child();
        child.setParent(parent);

        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setActive(false);

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(securityFacade.getCurrentUserId()).thenReturn(parentId);
        when(subscriptionPlanRepository.findById(planId)).thenReturn(Optional.of(plan));

        assertThrows(IllegalStateException.class, () -> subscriptionService.buySubscription(request));
    }

    @Test
    @DisplayName("Помилка при спробі купити абонемент на секцію, де вже є активний абонемент")
    void buySubscription_ThrowsException_WhenAlreadyHasActive() {
        UUID childId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        SubscriptionRequest request = new SubscriptionRequest(childId, planId);

        Parent parent = new Parent();
        parent.setId(parentId);
        Child child = new Child();
        child.setParent(parent);

        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setActive(true);
        plan.setSection(new Section());

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(securityFacade.getCurrentUserId()).thenReturn(parentId);
        when(subscriptionPlanRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(subscriptionRepository.existsByChildIdAndSubscriptionPlanSectionIdAndStatusIn(any(), any(), any())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> subscriptionService.buySubscription(request));
    }
}