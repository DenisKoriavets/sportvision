package com.github.deniskoriavets.sportvision.repository;

import com.github.deniskoriavets.sportvision.entity.Section;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SectionRepository extends JpaRepository<Section, UUID>,
    JpaSpecificationExecutor<Section> {
}
