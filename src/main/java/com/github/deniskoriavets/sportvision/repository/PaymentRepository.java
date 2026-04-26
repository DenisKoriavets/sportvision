package com.github.deniskoriavets.sportvision.repository;

import com.github.deniskoriavets.sportvision.entity.Payment;
import com.github.deniskoriavets.sportvision.entity.enums.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByStripeSessionId(String stripeSessionId);

    List<Payment> findAllByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime time);
}
