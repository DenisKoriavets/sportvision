package com.github.deniskoriavets.sportvision.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record GroupRequest(
    @NotBlank
    String name,
    @NotNull
    UUID sectionId,
    UUID coachId,
    @Min(1)
    Integer maxCapacity,
    Integer ageMin,
    Integer ageMax
) {}