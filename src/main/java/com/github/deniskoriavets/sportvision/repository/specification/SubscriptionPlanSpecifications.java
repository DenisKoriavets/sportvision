package com.github.deniskoriavets.sportvision.repository.specification;

import com.github.deniskoriavets.sportvision.dto.SubscriptionPlanSearchCriteria;
import com.github.deniskoriavets.sportvision.entity.SubscriptionPlan;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class SubscriptionPlanSpecifications {

    public static Specification<SubscriptionPlan> build(SubscriptionPlanSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.sectionId() != null) {
                predicates.add(cb.equal(root.get("section").get("id"), criteria.sectionId()));
            }
            if (criteria.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.minPrice()));
            }
            if (criteria.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.maxPrice()));
            }
            if (criteria.isUnlimited() != null) {
                predicates.add(cb.equal(root.get("isUnlimited"), criteria.isUnlimited()));
            }
            if (criteria.isActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), criteria.isActive()));
            }
            if (criteria.query() != null && !criteria.query().isBlank()) {
                String likePattern = "%" + criteria.query().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), likePattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
