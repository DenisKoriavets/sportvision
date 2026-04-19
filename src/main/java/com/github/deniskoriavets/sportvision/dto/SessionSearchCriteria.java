package com.github.deniskoriavets.sportvision.dto;

import com.github.deniskoriavets.sportvision.entity.enums.SessionStatus;
import java.time.LocalDate;
import java.util.UUID;

public record SessionSearchCriteria(
    UUID groupId,
    UUID sectionId,
    LocalDate startDate,
    LocalDate endDate,
    SessionStatus status
) {}