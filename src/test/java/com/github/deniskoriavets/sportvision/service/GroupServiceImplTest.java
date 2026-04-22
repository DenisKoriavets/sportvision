package com.github.deniskoriavets.sportvision.service;

import com.github.deniskoriavets.sportvision.dto.request.GroupRequest;
import com.github.deniskoriavets.sportvision.dto.response.GroupResponse;
import com.github.deniskoriavets.sportvision.entity.Group;
import com.github.deniskoriavets.sportvision.entity.Section;
import com.github.deniskoriavets.sportvision.mapper.GroupMapper;
import com.github.deniskoriavets.sportvision.repository.GroupRepository;
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
    @DisplayName("Creates group successfully")
    void createGroup_Success() {
        UUID sectionId = UUID.randomUUID();
        GroupRequest request = new GroupRequest("Група А", sectionId, null, 15, 7, 10);

        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(new Section()));
        when(groupMapper.toEntity(request)).thenReturn(new Group());
        when(groupRepository.save(any())).thenReturn(new Group());

        when(groupMapper.toResponseWithOccupancy(any(), anyInt())).thenReturn(
            new GroupResponse(UUID.randomUUID(), "Група А", sectionId, "Секція", "Тренер", 15, 0, 7, 10)
        );

        GroupResponse response = groupService.createGroup(request);

        assertNotNull(response);
        verify(groupMapper).toResponseWithOccupancy(any(), eq(0));
    }
}