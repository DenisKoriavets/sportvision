package com.github.deniskoriavets.sportvision;

import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.enums.Role;
import com.github.deniskoriavets.sportvision.repository.ParentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class BaseIntegrationTest {
    @Autowired
    private ParentRepository parentRepository;

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
}