package com.github.deniskoriavets.sportvision.dto.request;

import java.util.UUID;

public record EnrollmentRequest(
    UUID childId,
    UUID groupId
) {}