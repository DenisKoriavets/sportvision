package com.github.deniskoriavets.sportvision.entity;

import com.github.deniskoriavets.sportvision.entity.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "payments")
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(nullable = false)
    @ToString.Include
    private String stripeSessionId;

    private String stripeSessionUrl;

    @Column(nullable = false)
    @ToString.Include
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private PaymentStatus status;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
