package com.github.deniskoriavets.sportvision.dto;

import java.util.UUID;

public record GroupSearchCriteria(
    String query,
    UUID sectionId
) {}