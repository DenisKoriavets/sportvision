package com.github.deniskoriavets.sportvision.dto;

import com.github.deniskoriavets.sportvision.validation.ValidChildAge;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record ChildRequest(
    @NotBlank
    String firstName,
    @NotBlank
    String lastName,
    @ValidChildAge
    LocalDate birthDate
) {
}
