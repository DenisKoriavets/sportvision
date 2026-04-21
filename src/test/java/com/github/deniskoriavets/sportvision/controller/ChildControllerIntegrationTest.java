package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.BaseIntegrationTest;
import com.github.deniskoriavets.sportvision.dto.request.ChildRequest;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.enums.Role;
import com.github.deniskoriavets.sportvision.repository.ParentRepository;
import com.github.deniskoriavets.sportvision.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChildControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setUp() {
        parentRepository.deleteAll();
        Parent testParent = Parent.builder()
            .email("parent@test.com")
            .firstName("John")
            .lastName("Doe")
            .passwordHash("hash")
            .role(Role.PARENT)
            .isEmailVerified(true)
            .isActive(true)
            .build();
        testParent = parentRepository.save(testParent);
        token = "Bearer " + jwtService.generateAccessToken(testParent);
    }

    @Test
    @DisplayName("Успішне створення дитини")
    void createChild_Success() throws Exception {
        ChildRequest request = new ChildRequest("Олексій", "Іванов", LocalDate.now().minusYears(10));

        mockMvc.perform(post("/api/v1/children")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firstName").value("Олексій"));
    }
}