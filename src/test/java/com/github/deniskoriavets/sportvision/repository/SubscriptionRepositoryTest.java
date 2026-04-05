package com.github.deniskoriavets.sportvision.repository;

import com.github.deniskoriavets.sportvision.BaseIntegrationTest;
import com.github.deniskoriavets.sportvision.entity.*;
import com.github.deniskoriavets.sportvision.entity.enums.Role;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SubscriptionRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPlanRepository planRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Child testChild;
    private SubscriptionPlan testPlan;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE payments, subscriptions, subscription_plans, children, groups, sections, parents CASCADE");

        Parent parent = parentRepository.save(Parent.builder()
                .firstName("Alice")
                .lastName("Wonder")
                .email("alice@example.com")
                .passwordHash("hash")
                .role(Role.PARENT)
                .build());

        testChild = childRepository.save(Child.builder()
                .firstName("Charlie")
                .lastName("Wonder")
                .birthDate(LocalDate.of(2015, 1, 1))
                .parent(parent)
                .build());

        Section section = sectionRepository.save(Section.builder()
                .name("Swimming")
                .build());

        testPlan = planRepository.save(SubscriptionPlan.builder()
                .name("Standard 8")
                .section(section)
                .sessionsCount(8)
                .price(new BigDecimal("1000.00"))
                .validityDays(30)
                .build());
    }

    @Test
    @DisplayName("Should save subscription and increment version on update")
    void shouldIncrementVersionOnUpdate() {
        Subscription sub = subscriptionRepository.save(Subscription.builder()
                .child(testChild)
                .subscriptionPlan(testPlan)
                .totalSessions(8)
                .remainingSessions(8)
                .status(SubscriptionStatus.ACTIVE)
                .build());

        assertThat(sub.getVersion()).isEqualTo(0);

        sub.setRemainingSessions(7);
        Subscription updated = subscriptionRepository.save(sub);

        assertThat(updated.getVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should throw OptimisticLockingFailureException on concurrent update")
    void shouldThrowExceptionOnConcurrentUpdate() {
        Subscription sub = subscriptionRepository.save(Subscription.builder()
                .child(testChild)
                .subscriptionPlan(testPlan)
                .totalSessions(8)
                .remainingSessions(8)
                .status(SubscriptionStatus.ACTIVE)
                .build());

        UUID subId = sub.getId();

        Subscription thread1View = subscriptionRepository.findById(subId).orElseThrow();
        Subscription thread2View = subscriptionRepository.findById(subId).orElseThrow();

        thread1View.setRemainingSessions(7);
        subscriptionRepository.save(thread1View);

        thread2View.setRemainingSessions(6);
        
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            subscriptionRepository.save(thread2View);
        });
    }

    @Test
    @DisplayName("Should respect soft delete for subscriptions")
    void shouldRespectSoftDelete() {
        Subscription sub = subscriptionRepository.save(Subscription.builder()
                .child(testChild)
                .subscriptionPlan(testPlan)
                .totalSessions(4)
                .remainingSessions(4)
                .status(SubscriptionStatus.ACTIVE)
                .build());

        subscriptionRepository.delete(sub);

        assertThat(subscriptionRepository.findById(sub.getId())).isEmpty();

        Boolean isDeleted = jdbcTemplate.queryForObject(
            "SELECT is_deleted FROM subscriptions WHERE id = ?",
            Boolean.class, sub.getId()
        );
        assertThat(isDeleted).isTrue();
    }
}