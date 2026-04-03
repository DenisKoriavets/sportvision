package com.github.deniskoriavets.sportvision.repository;

import com.github.deniskoriavets.sportvision.BaseIntegrationTest;
import com.github.deniskoriavets.sportvision.entity.Child;
import com.github.deniskoriavets.sportvision.entity.Parent;
import com.github.deniskoriavets.sportvision.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChildRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Parent testParent;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE children, parents, refresh_tokens, verification_tokens CASCADE");

        testParent = parentRepository.save(Parent.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .passwordHash("hashed_password")
            .role(Role.PARENT)
            .isActive(true)
            .isEmailVerified(true)
            .build());
    }

    @Test
    @DisplayName("Should save child and find by parent id")
    void shouldSaveAndFindByParentId() {
        Child child = Child.builder()
            .firstName("Jane")
            .lastName("Doe")
            .birthDate(LocalDate.of(2016, 8, 12))
            .parent(testParent)
            .build();

        childRepository.save(child);
        List<Child> children = childRepository.findAllByParentId(testParent.getId());

        assertThat(children).hasSize(1);
        assertThat(children.get(0).getFirstName()).isEqualTo("Jane");
        assertThat(children.get(0).getLastName()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("Should not return deleted child due to soft delete")
    void shouldNotReturnDeletedChild() {
        Child child = childRepository.save(Child.builder()
            .firstName("Alice")
            .lastName("Smith")
            .birthDate(LocalDate.of(2018, 3, 20))
            .parent(testParent)
            .build());

        childRepository.delete(child);

        assertThat(childRepository.findById(child.getId())).isEmpty();
        assertThat(childRepository.findAllByParentId(testParent.getId())).isEmpty();
    }
}