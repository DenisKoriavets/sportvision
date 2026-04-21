package com.github.deniskoriavets.sportvision.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.deniskoriavets.sportvision.dto.request.EnrollmentRequest;
import com.github.deniskoriavets.sportvision.entity.Child;
import com.github.deniskoriavets.sportvision.entity.Group;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.repository.GroupRepository;
import com.github.deniskoriavets.sportvision.security.SecurityFacade;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    @Mock private GroupRepository groupRepository;
    @Mock private ChildRepository childRepository;
    @Mock private SecurityFacade securityFacade;

    @InjectMocks private EnrollmentServiceImpl enrollmentService;

    @Test
    void enrollChild_ShouldThrowException_WhenAgeIsTooLow() {
        UUID childId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        EnrollmentRequest request = new EnrollmentRequest(childId, groupId);

        Parent parent = new Parent();
        parent.setId(parentId);

        Child child = new Child();
        child.setParent(parent);
        child.setBirthDate(LocalDate.now().minusYears(5));

        Group group = new Group();
        group.setId(groupId);
        group.setAgeMin(7);

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(securityFacade.getCurrentUserId()).thenReturn(parentId);
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> enrollmentService.enrollChild(request));
        assertTrue(exception.getMessage().contains("age requirements"));
    }

    @Test
    void enrollChild_ShouldThrowException_WhenGroupIsFull() {
        UUID childId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        EnrollmentRequest request = new EnrollmentRequest(childId, groupId);

        Parent parent = new Parent();
        parent.setId(parentId);

        Child child = new Child();
        child.setParent(parent);
        child.setBirthDate(LocalDate.now().minusYears(10));

        Group group = new Group();
        group.setId(groupId);
        group.setAgeMin(5);
        group.setAgeMax(12);
        group.setMaxCapacity(10);

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(securityFacade.getCurrentUserId()).thenReturn(parentId);
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(childRepository.countByGroupId(groupId)).thenReturn(10);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> enrollmentService.enrollChild(request));
        assertEquals("Group is at full capacity", exception.getMessage());
    }
}