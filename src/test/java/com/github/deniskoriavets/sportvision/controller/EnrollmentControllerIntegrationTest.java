package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.BaseIntegrationTest;
import com.github.deniskoriavets.sportvision.dto.request.EnrollmentRequest;
import com.github.deniskoriavets.sportvision.entity.*;
import com.github.deniskoriavets.sportvision.entity.enums.Role;
import com.github.deniskoriavets.sportvision.repository.*;
import com.github.deniskoriavets.sportvision.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EnrollmentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ParentRepository parentRepository;
    @Autowired
    private ChildRepository childRepository;
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        groupRepository.deleteAll();
        childRepository.deleteAll();
        sectionRepository.deleteAll();
        parentRepository.deleteAll();
    }

    @Test
    @DisplayName("Помилка запису при невідповідності віку (409 Conflict)")
    void enrollChild_Fail_AgeMismatch() throws Exception {
        Parent parent = parentRepository.save(Parent.builder()
            .email("enroll@test.com").passwordHash("h").firstName("P").lastName("L")
            .role(Role.PARENT).isEmailVerified(true).isActive(true).build());

        Parent coach = parentRepository.save(Parent.builder()
            .email("coach@test.com").passwordHash("h").firstName("C").lastName("T")
            .role(Role.COACH).isEmailVerified(true).isActive(true).build());

        Child child = childRepository.save(Child.builder()
            .firstName("Small").lastName("Kid").birthDate(LocalDate.now().minusYears(5))
            .parent(parent).build());

        Section section = sectionRepository.save(Section.builder().name("Boxing").build());

        Group group = groupRepository.save(Group.builder()
            .name("Pro Group").section(section).coach(coach)
            .ageMin(10).ageMax(15).maxCapacity(10).isDeleted(false).build());

        String token = "Bearer " + jwtService.generateAccessToken(parent);
        EnrollmentRequest request = new EnrollmentRequest(child.getId(), group.getId());

        mockMvc.perform(post("/api/v1/enrollments")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }
}