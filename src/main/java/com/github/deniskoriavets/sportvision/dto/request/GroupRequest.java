package com.github.deniskoriavets.sportvision.dto.request;

import com.github.deniskoriavets.sportvision.validation.ValidAgePeriod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@ValidAgePeriod
public record GroupRequest(
    @NotBlank(message = "Group name must not be blank")
    String name,
    @NotNull(message = "Section ID must not be null")
    UUID sectionId,
    UUID coachId,
    @Min(value = 1, message = "Max capacity must be at least 1")
    Integer maxCapacity,
    Integer ageMin,
    Integer ageMax
) {}