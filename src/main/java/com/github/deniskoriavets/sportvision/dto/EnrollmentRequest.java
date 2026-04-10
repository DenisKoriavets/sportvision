package com.github.deniskoriavets.sportvision.dto;

import java.util.UUID;

public record EnrollmentRequest(
    UUID childId,
    UUID groupId
) {}