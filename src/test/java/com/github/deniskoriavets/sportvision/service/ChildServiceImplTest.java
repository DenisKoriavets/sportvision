package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.entity.Child;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.security.SecurityFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChildServiceImplTest {

    @Mock private ChildRepository childRepository;
    @Mock private SecurityFacade securityFacade;

    @InjectMocks private ChildServiceImpl childService;

    @Test
    @DisplayName("Помилка доступу при спробі отримати дані чужої дитини")
    void getChildById_ThrowsAccessDenied_WhenNotOwner() {
        UUID childId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        UUID otherParentId = UUID.randomUUID();

        Parent parent = new Parent();
        parent.setId(parentId);
        Child child = new Child();
        child.setParent(parent);

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(securityFacade.getCurrentUserId()).thenReturn(otherParentId);

        assertThrows(AccessDeniedException.class, () -> childService.getChildById(childId));
    }
}