package com.github.deniskoriavets.sportvision.repository;

import com.github.deniskoriavets.sportvision.entity.Session;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SessionRepository extends JpaRepository<Session, UUID>, JpaSpecificationExecutor<Session> {
    boolean existsByGroupIdAndDateAndStartTime(UUID groupId, LocalDate date, LocalTime startTime);

    List<Session> findAllByGroupIdAndDateBetween(UUID groupId, LocalDate startDate, LocalDate endDate);
}
