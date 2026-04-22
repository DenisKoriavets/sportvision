package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.BaseIntegrationTest;
import com.github.deniskoriavets.sportvision.dto.request.GroupRequest;
import com.github.deniskoriavets.sportvision.entity.Group;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.Section;
import com.github.deniskoriavets.sportvision.entity.enums.Role;
import com.github.deniskoriavets.sportvision.repository.GroupRepository;
import com.github.deniskoriavets.sportvision.repository.ParentRepository;
import com.github.deniskoriavets.sportvision.repository.SectionRepository;
import com.github.deniskoriavets.sportvision.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GroupControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private GroupRepository groupRepository;
    @Autowired private SectionRepository sectionRepository;
    @Autowired private ParentRepository parentRepository;
    @Autowired private JwtService jwtService;
    @Autowired private ObjectMapper objectMapper;

    private Section section;
    private Parent coach;
    private String adminToken;
    private String parentToken;

    @BeforeEach
    void setUp() {
        truncateAll();
        section = sectionRepository.save(Section.builder().name("Футбол").build());
        coach = createParent("coach@test.com", Role.COACH);
        Parent admin = createParent("admin@test.com", Role.ADMIN);
        Parent parent = createParent("parent@test.com", Role.PARENT);
        adminToken = "Bearer " + jwtService.generateAccessToken(admin);
        parentToken = "Bearer " + jwtService.generateAccessToken(parent);
    }

    @Test
    @DisplayName("ADMIN can create a group — returns 201")
    void createGroup_Success_AsAdmin() throws Exception {
        GroupRequest request = new GroupRequest("Група А", section.getId(), coach.getId(), 15, 7, 12);

        mockMvc.perform(post("/api/v1/groups")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Група А"));
    }

    @Test
    @DisplayName("PARENT cannot create a group — returns 403")
    void createGroup_Forbidden_AsParent() throws Exception {
        GroupRequest request = new GroupRequest("Група А", section.getId(), null, 15, 7, 12);

        mockMvc.perform(post("/api/v1/groups")
                .header("Authorization", parentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get groups is available to all authenticated users")
    void getGroups_Success() throws Exception {
        groupRepository.save(Group.builder()
            .name("Тестова Група").section(section).coach(coach).maxCapacity(10).build());

        mockMvc.perform(get("/api/v1/groups")
                .header("Authorization", parentToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("Validation @ValidAgePeriod — ageMin greater than ageMax returns 400")
    void createGroup_Fails_WhenAgeMinGreaterThanAgeMax() throws Exception {
        GroupRequest request = new GroupRequest("Погана Група", section.getId(), null, 10, 15, 7);

        mockMvc.perform(post("/api/v1/groups")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ADMIN can delete a group — returns 204")
    void deleteGroup_Success_AsAdmin() throws Exception {
        Group group = groupRepository.save(Group.builder()
            .name("Delete Me").section(section).coach(coach).maxCapacity(5).build());

        mockMvc.perform(delete("/api/v1/groups/" + group.getId())
                .header("Authorization", adminToken))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Get non-existent group — returns 404")
    void getGroupById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/groups/" + java.util.UUID.randomUUID())
                .header("Authorization", parentToken))
            .andExpect(status().isNotFound());
    }
}
