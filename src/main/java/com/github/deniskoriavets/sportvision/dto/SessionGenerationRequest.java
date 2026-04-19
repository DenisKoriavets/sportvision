package com.github.deniskoriavets.sportvision.dto;

import java.time.LocalDate;
import java.util.UUID;

public record SessionGenerationRequest(
    UUID groupId,
    LocalDate startDate,
    LocalDate endDate
) {
}
