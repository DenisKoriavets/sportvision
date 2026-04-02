package com.github.deniskoriavets.sportvision.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE subscription_plans SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(nullable = false)
    @ToString.Include
    private String name;

    private Integer sessionsCount;

    @Column(nullable = false)
    @Builder.Default
    private boolean isUnlimited = false;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer validityDays;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;
}