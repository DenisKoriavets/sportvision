package com.github.deniskoriavets.sportvision.repository;

import com.github.deniskoriavets.sportvision.entity.Schedule;
import com.github.deniskoriavets.sportvision.entity.enums.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    List<Schedule> findAllByGroupId(UUID groupId);

    @Query("""
        SELECT COUNT(s) > 0 FROM Schedule s 
        JOIN s.group g 
        WHERE g.coach.id = :coachId 
        AND s.dayOfWeek = :dayOfWeek 
        AND s.isDeleted = false 
        AND ((s.startTime < :endTime AND s.endTime > :startTime))
    """)
    boolean existsCoachConflict(
        @Param("coachId") UUID coachId,
        @Param("dayOfWeek") DayOfWeek dayOfWeek,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );
}
