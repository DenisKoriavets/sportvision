package com.github.deniskoriavets.sportvision.repository;

import com.github.deniskoriavets.sportvision.entity.SubscriptionPlan;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
}
