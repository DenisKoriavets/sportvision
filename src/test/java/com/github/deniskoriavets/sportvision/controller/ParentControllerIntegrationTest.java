package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.BaseIntegrationTest;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.enums.Role;
import com.github.deniskoriavets.sportvision.repository.ParentRepository;
import com.github.deniskoriavets.sportvision.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ParentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ParentRepository parentRepository;
    @Autowired private JwtService jwtService;

    @Test
    @DisplayName("Успішне отримання профілю через /me")
    void getMe_Success() throws Exception {
        Parent parent = parentRepository.save(Parent.builder()
                .email("denis@koriavets.com").firstName("Денис").lastName("Корявець")
                .role(Role.PARENT).isEmailVerified(true).isActive(true).passwordHash("h").build());
        
        String token = "Bearer " + jwtService.generateAccessToken(parent);

        mockMvc.perform(get("/api/v1/parents/me")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("denis@koriavets.com"))
                .andExpect(jsonPath("$.firstName").value("Денис"));
    }
}