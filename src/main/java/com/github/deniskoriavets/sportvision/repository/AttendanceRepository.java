package com.github.deniskoriavets.sportvision.repository;

import com.github.deniskoriavets.sportvision.entity.Attendance;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    boolean existsBySessionIdAndChildId(UUID sessionId, UUID childId);

    List<Attendance> findAllByChildId(UUID childId);

    List<Attendance> findAllBySessionId(UUID sessionId);
}
