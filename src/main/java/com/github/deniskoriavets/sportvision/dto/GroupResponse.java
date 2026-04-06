package com.github.deniskoriavets.sportvision.dto;

import java.util.UUID;

public record GroupResponse(
    UUID id,
    String name,
    UUID sectionId,
    String sectionName,
    String coachName,
    Integer maxCapacity,
    Integer currentOccupancy,
    Integer ageMin,
    Integer ageMax
) {
}
