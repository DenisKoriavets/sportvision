package com.github.deniskoriavets.sportvision.repository;

import com.github.deniskoriavets.sportvision.entity.Session;
import com.github.deniskoriavets.sportvision.entity.enums.SessionStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface SessionRepository extends JpaRepository<Session, UUID>, JpaSpecificationExecutor<Session> {
    boolean existsByGroupIdAndDateAndStartTime(UUID groupId, LocalDate date, LocalTime startTime);

    List<Session> findAllByGroupIdAndDateBetween(UUID groupId, LocalDate startDate, LocalDate endDate);

    List<Session> findAllByDateAndStatus(LocalDate date, SessionStatus status);

    @Query("SELECT s FROM Session s JOIN FETCH s.group WHERE s.id = :id")
    Optional<Session> findByIdWithGroup(UUID id);
}