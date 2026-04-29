package com.github.deniskoriavets.sportvision.repository;

import com.github.deniskoriavets.sportvision.entity.Payment;
import com.github.deniskoriavets.sportvision.entity.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByStripeSessionId(String stripeSessionId);

    List<Payment> findAllByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime time);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

    @Query("""
        SELECT p FROM Payment p
        JOIN p.subscription s
        JOIN s.child c
        WHERE c.parent.id = :parentId
        ORDER BY p.createdAt DESC
        """)
    List<Payment> findAllByParentId(@Param("parentId") UUID parentId);

    @Query("""
        SELECT p FROM Payment p
        JOIN p.subscription s
        JOIN s.child c
        WHERE p.id = :paymentId AND c.parent.id = :parentId
        """)
    Optional<Payment> findByIdAndParentId(
        @Param("paymentId") UUID paymentId,
        @Param("parentId") UUID parentId);
}
