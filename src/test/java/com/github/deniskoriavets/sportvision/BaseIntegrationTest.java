package com.github.deniskoriavets.sportvision;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.deniskoriavets.sportvision.dto.request.LoginRequest;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.enums.Role;
import com.github.deniskoriavets.sportvision.repository.ParentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class BaseIntegrationTest {

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    protected void truncateAll() {
        jdbcTemplate.execute(
            "TRUNCATE TABLE payments, subscriptions, subscription_plans, " +
                "attendances, sessions, schedules, children, groups, sections, " +
                "refresh_tokens, verification_tokens, parent_notification_preferences, parents CASCADE"
        );
    }

    protected Parent createParent(String email, Role role) {
        return parentRepository.save(Parent.builder()
            .email(email)
            .firstName("Test")
            .lastName("User")
            .passwordHash("hashed_password")
            .role(role)
            .isEmailVerified(true)
            .isActive(true)
            .build());
    }

    protected String loginAndGetToken(String email, String rawPassword) throws Exception {
        var result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(email, rawPassword))))
            .andExpect(status().isOk())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
            .get("accessToken").asText();
    }

    protected Parent createParentWithPassword(String email, Role role, String rawPassword) {
        return parentRepository.save(Parent.builder()
            .email(email).firstName("Test").lastName("User")
            .passwordHash(passwordEncoder.encode(rawPassword))
            .role(role).isEmailVerified(true).isActive(true)
            .build());
    }
}