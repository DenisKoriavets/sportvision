package com.github.deniskoriavets.sportvision.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.deniskoriavets.sportvision.BaseIntegrationTest;
import com.github.deniskoriavets.sportvision.dto.ChildAttendanceDto;
import com.github.deniskoriavets.sportvision.dto.request.BulkAttendanceRequest;
import com.github.deniskoriavets.sportvision.entity.Child;
import com.github.deniskoriavets.sportvision.entity.Group;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.Section;
import com.github.deniskoriavets.sportvision.entity.Session;
import com.github.deniskoriavets.sportvision.entity.Subscription;
import com.github.deniskoriavets.sportvision.entity.SubscriptionPlan;
import com.github.deniskoriavets.sportvision.entity.enums.AttendanceStatus;
import com.github.deniskoriavets.sportvision.entity.enums.Role;
import com.github.deniskoriavets.sportvision.entity.enums.SessionStatus;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.repository.GroupRepository;
import com.github.deniskoriavets.sportvision.repository.ParentRepository;
import com.github.deniskoriavets.sportvision.repository.SectionRepository;
import com.github.deniskoriavets.sportvision.repository.SessionRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionPlanRepository;
import com.github.deniskoriavets.sportvision.repository.SubscriptionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

public class AttendanceControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ChildRepository childRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private ParentRepository parentRepository;
    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    public void tearDown() {
        truncateAll();
    }

    @Test
    @DisplayName("Should deduct session from active subscription after attendance is marked")
    @WithMockUser(username = "parent@test.com", roles = "COACH")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void markAttendance_ShouldDeductSessionFromSubscription() throws Exception {
        var parent = Parent.builder().firstName("Ivan").lastName("Ivanov").email("parent@test.com")
            .passwordHash("123123123").phone("+380991112233").role(Role.COACH).build();
        parentRepository.save(parent);
        var section = Section.builder().name("Section 1").build();
        sectionRepository.save(section);
        var subscriptionPlan = SubscriptionPlan.builder().name("10 Sessions").sessionsCount(10)
            .price(BigDecimal.valueOf(1000)).section(section).validityDays(30).build();
        subscriptionPlanRepository.save(subscriptionPlan);
        var group = Group.builder().name("Group 1").section(section).maxCapacity(10).ageMax(11)
            .coach(parent).build();
        groupRepository.save(group);
        var child = Child.builder().firstName("Danylo").lastName("Ivanov").birthDate(
            LocalDate.now().minusYears(10)).parent(parent).build();
        childRepository.save(child);
        var subscription = Subscription.builder().child(child).subscriptionPlan(subscriptionPlan)
            .remainingSessions(10).totalSessions(subscriptionPlan.getSessionsCount())
            .status(SubscriptionStatus.ACTIVE).build();
        subscriptionRepository.save(subscription);
        var session = Session.builder().startTime(LocalTime.now()).endTime(LocalTime.now().plusHours(1)).group(group).status(
            SessionStatus.SCHEDULED).date(LocalDate.now()).build();
        sessionRepository.save(session);

        var attendanceList = List.of(new ChildAttendanceDto(child.getId(), AttendanceStatus.PRESENT));

        mockMvc.perform(post("/api/v1/sessions/{id}/attendance", session.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attendanceList))
                .with(user(parent)))
            .andExpect(status().isOk());

        subscription = subscriptionRepository.findById(subscription.getId()).orElse(null);
        assert subscription != null;
        assertEquals(9, subscription.getRemainingSessions());
    }
}
