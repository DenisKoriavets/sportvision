package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.BaseIntegrationTest;
import com.github.deniskoriavets.sportvision.dto.SubscriptionRequest;
import com.github.deniskoriavets.sportvision.entity.*;
import com.github.deniskoriavets.sportvision.entity.enums.Role;
import com.github.deniskoriavets.sportvision.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SubscriptionControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ParentRepository parentRepository;
    @Autowired private ChildRepository childRepository;
    @Autowired private SectionRepository sectionRepository;
    @Autowired private SubscriptionPlanRepository planRepository;
    @Autowired private com.github.deniskoriavets.sportvision.security.JwtService jwtService;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("Успішна покупка абонемента")
    void buySubscription_Success() throws Exception {
        Parent parent = parentRepository.save(Parent.builder()
            .email("sub@test.com")
            .passwordHash("h")
            .firstName("John") // ОБОВ'ЯЗКОВО
            .lastName("Doe")   // ОБОВ'ЯЗКОВО
            .role(Role.PARENT) // ОБОВ'ЯЗКОВО
            .isEmailVerified(true)
            .isActive(true)
            .build());

        Child child = childRepository.save(Child.builder()
            .firstName("Kid").lastName("Test").birthDate(LocalDate.now().minusYears(8)).parent(parent).build());

        Section section = sectionRepository.save(Section.builder().name("Swimming").build());

        SubscriptionPlan plan = planRepository.save(SubscriptionPlan.builder()
            .name("8 Sessions").price(new BigDecimal("1200")).sessionsCount(8)
            .validityDays(30).isActive(true).section(section).isUnlimited(false).build());

        String token = "Bearer " + jwtService.generateAccessToken(parent);
        SubscriptionRequest request = new SubscriptionRequest(child.getId(), plan.getId());

        mockMvc.perform(post("/api/v1/subscriptions/buy")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}