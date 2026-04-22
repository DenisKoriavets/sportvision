package com.github.deniskoriavets.sportvision.dto.request;

import com.github.deniskoriavets.sportvision.validation.ValidChildAge;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record ChildRequest(
    @NotBlank(message = "First name must not be blank")
    String firstName,
    @NotBlank(message = "Last name must not be blank")
    String lastName,
    @ValidChildAge
    LocalDate birthDate
) {
}
