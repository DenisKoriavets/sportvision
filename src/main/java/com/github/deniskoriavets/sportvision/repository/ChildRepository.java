package com.github.deniskoriavets.sportvision.repository;

import com.github.deniskoriavets.sportvision.entity.Child;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ChildRepository extends JpaRepository<Child, UUID>,
    JpaSpecificationExecutor<Child> {
    List<Child> findAllByParentId(UUID parentId);

    Integer countByGroupId(UUID groupId);

    List<Child> findAllByGroupId(UUID groupId);

    @Query("""
    SELECT c FROM Child c
    JOIN FETCH c.parent p
    JOIN FETCH p.notificationPreferences
    WHERE c.id = :id
    """)
    Optional<Child> findByIdWithParentAndPreferences(UUID id);

    @Query("""
    SELECT c FROM Child c
    JOIN FETCH c.parent p
    JOIN FETCH p.notificationPreferences
    WHERE c.group.id = :groupId
    """)
    List<Child> findAllByGroupIdWithParentAndPreferences(UUID groupId);
}