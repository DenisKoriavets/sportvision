package com.github.deniskoriavets.sportvision.repository.specification;

import com.github.deniskoriavets.sportvision.dto.GroupSearchCriteria;
import com.github.deniskoriavets.sportvision.entity.Group;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class GroupSpecifications {

    public static Specification<Group> build(GroupSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.query() != null && !criteria.query().isBlank()) {
                String searchPattern = "%" + criteria.query().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), searchPattern));
            }

            if (criteria.sectionId() != null) {
                predicates.add(cb.equal(root.get("section").get("id"), criteria.sectionId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}