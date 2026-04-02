package com.github.deniskoriavets.sportvision.repository;

import com.github.deniskoriavets.sportvision.entity.Payment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
