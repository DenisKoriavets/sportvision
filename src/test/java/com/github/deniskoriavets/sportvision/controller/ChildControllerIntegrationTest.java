package com.github.deniskoriavets.sportvision.controller;

import com.github.deniskoriavets.sportvision.BaseIntegrationTest;
import com.github.deniskoriavets.sportvision.dto.request.ChildRequest;
import com.github.deniskoriavets.sportvision.entity.Child;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.enums.Role;
import com.github.deniskoriavets.sportvision.repository.ChildRepository;
import com.github.deniskoriavets.sportvision.repository.ParentRepository;
import com.github.deniskoriavets.sportvision.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChildControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ParentRepository parentRepository;
    @Autowired private ChildRepository childRepository;
    @Autowired private JwtService jwtService;
    @Autowired private ObjectMapper objectMapper;

    private Parent parent;
    private String token;

    @BeforeEach
    void setUp() {
        truncateAll();
        parent = createParent("parent@test.com", Role.PARENT);
        token = "Bearer " + jwtService.generateAccessToken(parent);
    }

    @Test
    @DisplayName("Create child successfully — returns 201")
    void createChild_Success() throws Exception {
        ChildRequest request = new ChildRequest("Олексій", "Іванов", LocalDate.now().minusYears(10));

        mockMvc.perform(post("/api/v1/children")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firstName").value("Олексій"))
            .andExpect(jsonPath("$.lastName").value("Іванов"));
    }

    @Test
    @DisplayName("Validation — blank first name returns 400")
    void createChild_Fails_WhenFirstNameBlank() throws Exception {
        ChildRequest request = new ChildRequest("", "Іванов", LocalDate.now().minusYears(10));

        mockMvc.perform(post("/api/v1/children")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get all children returns only current user's children")
    void getAllChildren_ReturnsOwnChildren() throws Exception {
        childRepository.save(Child.builder()
            .firstName("Марія").lastName("Іванова")
            .birthDate(LocalDate.now().minusYears(8)).parent(parent).build());

        mockMvc.perform(get("/api/v1/children")
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].firstName").value("Марія"));
    }

    @Test
    @DisplayName("Get child by ID — returns 200")
    void getChildById_Success() throws Exception {
        Child child = childRepository.save(Child.builder()
            .firstName("Петро").lastName("Коваль")
            .birthDate(LocalDate.now().minusYears(7)).parent(parent).build());

        mockMvc.perform(get("/api/v1/children/" + child.getId())
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Петро"));
    }

    @Test
    @DisplayName("Get another user's child — returns 403")
    void getChildById_Forbidden_WhenNotOwner() throws Exception {
        Parent otherParent = createParent("other@test.com", Role.PARENT);
        Child otherChild = childRepository.save(Child.builder()
            .firstName("Чужий").lastName("Дитина")
            .birthDate(LocalDate.now().minusYears(7)).parent(otherParent).build());

        mockMvc.perform(get("/api/v1/children/" + otherChild.getId())
                .header("Authorization", token))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Update child — returns 200")
    void updateChild_Success() throws Exception {
        Child child = childRepository.save(Child.builder()
            .firstName("Старе").lastName("Ім'я")
            .birthDate(LocalDate.now().minusYears(7)).parent(parent).build());
        ChildRequest update = new ChildRequest("Нове", "Ім'я", LocalDate.now().minusYears(8));

        mockMvc.perform(put("/api/v1/children/" + child.getId())
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Нове"));
    }

    @Test
    @DisplayName("Delete child — returns 204")
    void deleteChild_Success() throws Exception {
        Child child = childRepository.save(Child.builder()
            .firstName("Delete").lastName("Me")
            .birthDate(LocalDate.now().minusYears(7)).parent(parent).build());

        mockMvc.perform(delete("/api/v1/children/" + child.getId())
                .header("Authorization", token))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Request without token — returns 403")
    void anyRequest_Without_Token_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/children"))
            .andExpect(status().isForbidden());
    }
}
