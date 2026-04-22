package com.github.deniskoriavets.sportvision.repository.specification;

import com.github.deniskoriavets.sportvision.dto.criteria.SectionSearchCriteria;
import com.github.deniskoriavets.sportvision.entity.Section;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class SectionSpecifications {

    public static Specification<Section> build(SectionSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.query() != null && !criteria.query().isBlank()) {
                String searchPattern = "%" + criteria.query().toLowerCase() + "%";
                
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), searchPattern);
                Predicate descPredicate = cb.like(cb.lower(root.get("description")), searchPattern);
                
                predicates.add(cb.or(namePredicate, descPredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}