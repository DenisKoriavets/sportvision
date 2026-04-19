package com.github.deniskoriavets.sportvision.dto;

import java.util.UUID;

public record ChildSearchCriteria(
    String query,
    UUID groupId,
    UUID parentId
) {}