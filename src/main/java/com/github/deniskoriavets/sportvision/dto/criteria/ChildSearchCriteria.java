package com.github.deniskoriavets.sportvision.dto.criteria;

import java.util.UUID;

public record ChildSearchCriteria(
    String query,
    UUID groupId,
    UUID parentId
) {}