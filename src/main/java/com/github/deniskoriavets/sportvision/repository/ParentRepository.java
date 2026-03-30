package com.github.deniskoriavets.sportvision.repository;

import com.github.deniskoriavets.sportvision.entity.Parent;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface ParentRepository extends JpaRepository<Parent, UUID> {
  Optional<Parent> findByEmail(String email);

  boolean existsByEmail(String email);
}
