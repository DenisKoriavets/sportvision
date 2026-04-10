package com.github.deniskoriavets.sportvision.repository;

import com.github.deniskoriavets.sportvision.entity.Subscription;
import com.github.deniskoriavets.sportvision.entity.enums.SubscriptionStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    boolean existsByChildIdAndSubscriptionPlanSectionIdAndStatusIn(
        UUID childId,
        UUID sectionId,
        Collection<SubscriptionStatus> statuses
    );

    List<Subscription> findAllByChildId(UUID childId);

    boolean existsByChildIdAndSubscriptionPlanSectionIdAndStatus(
        UUID childId,
        UUID sectionId,
        SubscriptionStatus status
    );

    Optional<Subscription> findFirstByChildIdAndSubscriptionPlanSectionIdAndStatus(UUID childId, UUID sectionId, SubscriptionStatus status);
}
