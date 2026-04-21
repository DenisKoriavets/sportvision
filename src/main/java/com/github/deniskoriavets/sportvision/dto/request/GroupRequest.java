package com.github.deniskoriavets.sportvision.dto.request;

import com.github.deniskoriavets.sportvision.validation.ValidAgePeriod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@ValidAgePeriod
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