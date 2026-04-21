package com.github.deniskoriavets.sportvision.repository.specification;

import com.github.deniskoriavets.sportvision.dto.criteria.SessionSearchCriteria;
import com.github.deniskoriavets.sportvision.entity.Session;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class SessionSpecifications {

    public static Specification<Session> build(SessionSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.groupId() != null) {
                predicates.add(cb.equal(root.get("group").get("id"), criteria.groupId()));
            }

            if (criteria.sectionId() != null) {
                predicates.add(cb.equal(root.get("group").get("section").get("id"), criteria.sectionId()));
            }

            if (criteria.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), criteria.startDate()));
            }

            if (criteria.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), criteria.endDate()));
            }

            if (criteria.status() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.status()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}