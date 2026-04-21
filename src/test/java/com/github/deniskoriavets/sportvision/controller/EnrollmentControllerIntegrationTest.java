package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.BaseIntegrationTest;
import com.github.deniskoriavets.sportvision.dto.request.EnrollmentRequest;
import com.github.deniskoriavets.sportvision.entity.*;
import com.github.deniskoriavets.sportvision.entity.enums.Role;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import com.github.deniskoriavets.sportvision.repository.*;
import com.github.deniskoriavets.sportvision.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EnrollmentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ParentRepository parentRepository;
    @Autowired private ChildRepository childRepository;
    @Autowired private SectionRepository sectionRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private SubscriptionPlanRepository planRepository;
    @Autowired private SubscriptionRepository subscriptionRepository;
    @Autowired private JwtService jwtService;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        truncateAll();
    }

    @Test
    @DisplayName("Успішний запис дитини в групу — 200")
    void enrollChild_Success() throws Exception {
        Parent parent = createParent("enroll-ok@test.com", Role.PARENT);
        Parent coach = createParent("coach-ok@test.com", Role.COACH);
        Section section = sectionRepository.save(Section.builder().name("Swimming").build());
        Group group = groupRepository.save(Group.builder()
            .name("Група A").section(section).coach(coach)
            .ageMin(5).ageMax(15).maxCapacity(10).build());

        Child child = childRepository.save(Child.builder()
            .firstName("Валід").lastName("Дитина")
            .birthDate(LocalDate.now().minusYears(10)).parent(parent).build());

        SubscriptionPlan plan = planRepository.save(SubscriptionPlan.builder()
            .name("8 занять").price(new BigDecimal("500")).sessionsCount(8)
            .validityDays(30).isActive(true).isUnlimited(false).section(section).build());

        subscriptionRepository.save(Subscription.builder()
            .child(child).subscriptionPlan(plan)
            .totalSessions(8).remainingSessions(8)
            .status(SubscriptionStatus.ACTIVE)
            .startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(30))
            .build());

        String token = "Bearer " + jwtService.generateAccessToken(parent);
        EnrollmentRequest request = new EnrollmentRequest(child.getId(), group.getId());

        mockMvc.perform(post("/api/v1/enrollments")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Помилка запису при невідповідності віку — 409")
    void enrollChild_Fails_AgeMismatch() throws Exception {
        Parent parent = createParent("enroll@test.com", Role.PARENT);
        Parent coach = createParent("coach@test.com", Role.COACH);
        Section section = sectionRepository.save(Section.builder().name("Boxing").build());
        Group group = groupRepository.save(Group.builder()
            .name("Pro Group").section(section).coach(coach)
            .ageMin(10).ageMax(15).maxCapacity(10).build());

        Child child = childRepository.save(Child.builder()
            .firstName("Small").lastName("Kid")
            .birthDate(LocalDate.now().minusYears(5)).parent(parent).build());

        String token = "Bearer " + jwtService.generateAccessToken(parent);
        EnrollmentRequest request = new EnrollmentRequest(child.getId(), group.getId());

        mockMvc.perform(post("/api/v1/enrollments")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PARENT не може записати чужу дитину — 403")
    void enrollChild_Fails_WhenNotOwner() throws Exception {
        Parent parent = createParent("owner@test.com", Role.PARENT);
        Parent otherParent = createParent("other@test.com", Role.PARENT);
        Parent coach = createParent("coach2@test.com", Role.COACH);
        Section section = sectionRepository.save(Section.builder().name("Tennis").build());
        Group group = groupRepository.save(Group.builder()
            .name("Group B").section(section).coach(coach)
            .ageMin(5).ageMax(15).maxCapacity(10).build());

        Child child = childRepository.save(Child.builder()
            .firstName("Чужа").lastName("Дитина")
            .birthDate(LocalDate.now().minusYears(10)).parent(otherParent).build());

        String token = "Bearer " + jwtService.generateAccessToken(parent);
        EnrollmentRequest request = new EnrollmentRequest(child.getId(), group.getId());

        mockMvc.perform(post("/api/v1/enrollments")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }
}
