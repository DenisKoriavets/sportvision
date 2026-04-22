package com.github.deniskoriavets.sportvision.repository.specification;

import com.github.deniskoriavets.sportvision.dto.criteria.GroupSearchCriteria;
import com.github.deniskoriavets.sportvision.entity.Child;
import com.github.deniskoriavets.sportvision.entity.Group;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
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

            if (criteria.hasAvailableSlots() != null && criteria.hasAvailableSlots()) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<Child> childRoot = subquery.from(Child.class);
                subquery.select(cb.count(childRoot));
                subquery.where(cb.equal(childRoot.get("group"), root));

                predicates.add(cb.greaterThan(root.get("maxCapacity"), subquery));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}