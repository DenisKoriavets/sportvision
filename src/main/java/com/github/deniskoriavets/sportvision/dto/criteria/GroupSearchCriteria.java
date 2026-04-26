package com.github.deniskoriavets.sportvision.dto.criteria;

import java.util.UUID;

public record GroupSearchCriteria(
    String query,
    UUID sectionId,
    UUID coachId,
    Boolean hasAvailableSlots
) {}