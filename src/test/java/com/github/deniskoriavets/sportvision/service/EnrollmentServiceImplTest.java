package com.github.deniskoriavets.sportvision.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.deniskoriavets.sportvision.dto.request.EnrollmentRequest;
import com.github.deniskoriavets.sportvision.entity.Child;
import com.github.deniskoriavets.sportvision.entity.Group;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.Section;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.repository.GroupRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionRepository;
import com.github.deniskoriavets.sportvision.security.SecurityFacade;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    @Mock private GroupRepository groupRepository;
    @Mock private ChildRepository childRepository;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private SecurityFacade securityFacade;

    @InjectMocks private EnrollmentServiceImpl enrollmentService;

    private Child buildChild(UUID parentId, int ageYears) {
        Parent parent = new Parent();
        parent.setId(parentId);
        Child child = new Child();
        child.setParent(parent);
        child.setBirthDate(LocalDate.now().minusYears(ageYears));
        return child;
    }

    private Group buildGroup(int ageMin, int ageMax, int maxCapacity) {
        Section section = new Section();
        section.setId(UUID.randomUUID());
        Group group = new Group();
        group.setId(UUID.randomUUID());
        group.setAgeMin(ageMin);
        group.setAgeMax(ageMax);
        group.setMaxCapacity(maxCapacity);
        group.setSection(section);
        return group;
    }

    @Test
    @DisplayName("Успішний запис дитини в групу")
    void enrollChild_Success() {
        UUID parentId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();
        Child child = buildChild(parentId, 10);
        Group group = buildGroup(5, 15, 10);

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(securityFacade.getCurrentUserId()).thenReturn(parentId);
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(childRepository.countByGroupId(group.getId())).thenReturn(5);
        when(subscriptionRepository.existsByChildIdAndSubscriptionPlanSectionIdAndStatus(
            any(), any(), eq(SubscriptionStatus.ACTIVE))).thenReturn(true);

        assertDoesNotThrow(() -> enrollmentService.enrollChild(new EnrollmentRequest(childId, group.getId())));
        verify(childRepository).save(child);
    }

    @Test
    @DisplayName("Помилка при занадто малому віці дитини")
    void enrollChild_ThrowsException_WhenTooYoung() {
        UUID parentId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();
        Child child = buildChild(parentId, 5);
        Group group = buildGroup(7, 12, 10);

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(securityFacade.getCurrentUserId()).thenReturn(parentId);
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        assertThrows(IllegalStateException.class,
            () -> enrollmentService.enrollChild(new EnrollmentRequest(childId, group.getId())));
    }

    @Test
    @DisplayName("Помилка при занадто великому віці дитини")
    void enrollChild_ThrowsException_WhenTooOld() {
        UUID parentId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();
        Child child = buildChild(parentId, 16);
        Group group = buildGroup(7, 12, 10);

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(securityFacade.getCurrentUserId()).thenReturn(parentId);
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        assertThrows(IllegalStateException.class,
            () -> enrollmentService.enrollChild(new EnrollmentRequest(childId, group.getId())));
    }

    @Test
    @DisplayName("Помилка коли група заповнена")
    void enrollChild_ThrowsException_WhenGroupFull() {
        UUID parentId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();
        Child child = buildChild(parentId, 10);
        Group group = buildGroup(5, 15, 10);

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(securityFacade.getCurrentUserId()).thenReturn(parentId);
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(childRepository.countByGroupId(group.getId())).thenReturn(10);

        assertThrows(IllegalStateException.class,
            () -> enrollmentService.enrollChild(new EnrollmentRequest(childId, group.getId())));
    }

    @Test
    @DisplayName("Помилка коли немає активного абонементу")
    void enrollChild_ThrowsException_WhenNoActiveSubscription() {
        UUID parentId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();
        Child child = buildChild(parentId, 10);
        Group group = buildGroup(5, 15, 10);

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(securityFacade.getCurrentUserId()).thenReturn(parentId);
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(childRepository.countByGroupId(group.getId())).thenReturn(5);
        when(subscriptionRepository.existsByChildIdAndSubscriptionPlanSectionIdAndStatus(
            any(), any(), eq(SubscriptionStatus.ACTIVE))).thenReturn(false);

        assertThrows(IllegalStateException.class,
            () -> enrollmentService.enrollChild(new EnrollmentRequest(childId, group.getId())));
    }

    @Test
    @DisplayName("Помилка доступу коли не власник дитини")
    void enrollChild_ThrowsAccessDenied_WhenNotOwner() {
        UUID childId = UUID.randomUUID();
        Child child = buildChild(UUID.randomUUID(), 10);

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(securityFacade.getCurrentUserId()).thenReturn(UUID.randomUUID());

        assertThrows(AccessDeniedException.class,
            () -> enrollmentService.enrollChild(new EnrollmentRequest(childId, UUID.randomUUID())));
    }
}
