package com.github.deniskoriavets.sportvision.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ChildResponse(
    UUID id,
    String firstName,
    String lastName,
    LocalDate birthDate,
    UUID groupId
) {
}
