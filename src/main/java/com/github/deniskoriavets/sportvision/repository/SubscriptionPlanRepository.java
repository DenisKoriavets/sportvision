package com.github.deniskoriavets.sportvision.repository;

import com.github.deniskoriavets.sportvision.entity.SubscriptionPlan;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID>,
    JpaSpecificationExecutor<SubscriptionPlan> {
}
