package com.github.deniskoriavets.sportvision.dto.request;

import java.time.LocalDate;
import java.util.UUID;

public record SessionGenerationRequest(
    UUID groupId,
    LocalDate startDate,
    LocalDate endDate
) {
}
