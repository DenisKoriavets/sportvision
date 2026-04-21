package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.GroupRequest;
import com.github.deniskoriavets.sportvision.dto.GroupResponse;
import com.github.deniskoriavets.sportvision.entity.Group;
import com.github.deniskoriavets.sportvision.entity.Section;
import com.github.deniskoriavets.sportvision.mapper.GroupMapper;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.repository.GroupRepository;
import com.github.deniskoriavets.sportvision.repository.ParentRepository;
import com.github.deniskoriavets.sportvision.repository.SectionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    @Mock private GroupRepository groupRepository;
    @Mock private GroupMapper groupMapper;
    @Mock private SectionRepository sectionRepository;

    @InjectMocks private GroupServiceImpl groupService;

    @Test
    @DisplayName("Успішне створення групи")
    void createGroup_Success() {
        UUID sectionId = UUID.randomUUID();
        GroupRequest request = new GroupRequest("Група А", sectionId, null, 15, 7, 10);

        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(new Section()));
        when(groupMapper.toEntity(request)).thenReturn(new Group());
        when(groupRepository.save(any())).thenReturn(new Group());
        when(groupMapper.toResponse(any(), anyInt())).thenReturn(mock(GroupResponse.class));

        assertNotNull(groupService.createGroup(request));
        verify(groupRepository).save(any());
    }
}