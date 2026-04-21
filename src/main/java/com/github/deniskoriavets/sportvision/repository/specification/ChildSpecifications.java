package com.github.deniskoriavets.sportvision.repository.specification;

import com.github.deniskoriavets.sportvision.dto.criteria.ChildSearchCriteria;
import com.github.deniskoriavets.sportvision.entity.Child;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class ChildSpecifications {

    public static Specification<Child> build(ChildSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.query() != null && !criteria.query().isBlank()) {
                String pattern = "%" + criteria.query().toLowerCase() + "%";
                Predicate firstNameMatch = cb.like(cb.lower(root.get("firstName")), pattern);
                Predicate lastNameMatch = cb.like(cb.lower(root.get("lastName")), pattern);
                predicates.add(cb.or(firstNameMatch, lastNameMatch));
            }

            if (criteria.groupId() != null) {
                predicates.add(cb.equal(root.get("group").get("id"), criteria.groupId()));
            }

            if (criteria.parentId() != null) {
                predicates.add(cb.equal(root.get("parent").get("id"), criteria.parentId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}