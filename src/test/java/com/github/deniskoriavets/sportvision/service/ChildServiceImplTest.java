package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.request.ChildRequest;
import com.github.deniskoriavets.sportvision.dto.response.ChildResponse;
import com.github.deniskoriavets.sportvision.entity.Child;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.exception.ResourceNotFoundException;
import com.github.deniskoriavets.sportvision.mapper.AttendanceMapper;
import com.github.deniskoriavets.sportvision.mapper.ChildMapper;
import com.github.deniskoriavets.sportvision.mapper.SubscriptionMapper;
import com.github.deniskoriavets.sportvision.repository.AttendanceRepository;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionRepository;
import com.github.deniskoriavets.sportvision.security.SecurityFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChildServiceImplTest {

    @Mock private ChildRepository childRepository;
    @Mock private ChildMapper childMapper;
    @Mock private SecurityFacade securityFacade;

    @InjectMocks private ChildServiceImpl childService;

    @Test
    @DisplayName("Creates child and assigns current parent successfully")
    void createChild_Success() {
        ChildRequest request = new ChildRequest("Олексій", "Іванов", LocalDate.now().minusYears(8));
        Parent parent = new Parent();
        Child child = new Child();
        ChildResponse response = mock(ChildResponse.class);

        when(securityFacade.getCurrentUser()).thenReturn(parent);
        when(childMapper.toEntity(request)).thenReturn(child);
        when(childRepository.save(child)).thenReturn(child);
        when(childMapper.toResponse(child)).thenReturn(response);

        ChildResponse result = childService.createChild(request);

        assertNotNull(result);
        assertEquals(parent, child.getParent());
        verify(childRepository).save(child);
    }

    @Test
    @DisplayName("Updates child data successfully")
    void updateChild_Success() {
        UUID childId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        ChildRequest request = new ChildRequest("Нове Ім'я", "Нове Прізвище", LocalDate.now().minusYears(9));

        Parent parent = new Parent();
        parent.setId(parentId);
        Child child = new Child();
        child.setParent(parent);

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(securityFacade.getCurrentUserId()).thenReturn(parentId);
        when(childRepository.save(child)).thenReturn(child);
        when(childMapper.toResponse(child)).thenReturn(mock(ChildResponse.class));

        assertDoesNotThrow(() -> childService.updateChild(childId, request));
        assertEquals("Нове Ім'я", child.getFirstName());
        assertEquals("Нове Прізвище", child.getLastName());
    }

    @Test
    @DisplayName("Deletes child successfully")
    void deleteChild_Success() {
        UUID childId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        Parent parent = new Parent();
        parent.setId(parentId);
        Child child = new Child();
        child.setParent(parent);

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(securityFacade.getCurrentUserId()).thenReturn(parentId);

        assertDoesNotThrow(() -> childService.deleteChild(childId));
        verify(childRepository).delete(child);
    }

    @Test
    @DisplayName("Throws AccessDeniedException when accessing another user's child")
    void getChildById_ThrowsAccessDenied_WhenNotOwner() {
        UUID childId = UUID.randomUUID();
        Parent parent = new Parent();
        parent.setId(UUID.randomUUID());
        Child child = new Child();
        child.setParent(parent);

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(securityFacade.getCurrentUserId()).thenReturn(UUID.randomUUID());

        assertThrows(AccessDeniedException.class, () -> childService.getChildById(childId));
    }

    @Test
    @DisplayName("Throws AccessDeniedException when updating another user's child")
    void updateChild_ThrowsAccessDenied_WhenNotOwner() {
        UUID childId = UUID.randomUUID();
        Parent parent = new Parent();
        parent.setId(UUID.randomUUID());
        Child child = new Child();
        child.setParent(parent);

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(securityFacade.getCurrentUserId()).thenReturn(UUID.randomUUID());

        assertThrows(AccessDeniedException.class,
            () -> childService.updateChild(childId, new ChildRequest("A", "B", LocalDate.now().minusYears(5))));
    }

    @Test
    @DisplayName("Admin can get any child without ownership check")
    void getChildByIdAdmin_ReturnsChild_WithoutOwnershipCheck() {
        UUID childId = UUID.randomUUID();
        Child child = Child.builder().id(childId).firstName("Max").build();

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));
        when(childMapper.toResponse(child)).thenReturn(new ChildResponse(childId, "Max", null, null, null));

        ChildResponse result = childService.getChildByIdAdmin(childId);

        assertThat(result.id()).isEqualTo(childId);
        verifyNoInteractions(securityFacade);
    }

    @Test
    @DisplayName("Admin deleteChildAdmin removes child regardless of parent")
    void deleteChildAdmin_DeletesChild_WithoutOwnershipCheck() {
        UUID childId = UUID.randomUUID();
        Child child = Child.builder().id(childId).build();

        when(childRepository.findById(childId)).thenReturn(Optional.of(child));

        childService.deleteChildAdmin(childId);

        verify(childRepository).delete(child);
        verifyNoInteractions(securityFacade);
    }

    @Test
    @DisplayName("Admin deleteChildAdmin throws when child not found")
    void deleteChildAdmin_Throws_WhenNotFound() {
        UUID childId = UUID.randomUUID();
        when(childRepository.findById(childId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> childService.deleteChildAdmin(childId))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
