package com.github.deniskoriavets.sportvision.repository;

import com.github.deniskoriavets.sportvision.entity.Child;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ChildRepository extends JpaRepository<Child, UUID>,
    JpaSpecificationExecutor<Child> {
    List<Child> findAllByParentId(UUID parentId);

    Integer countByGroupId(UUID groupId);
}