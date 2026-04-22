package com.github.deniskoriavets.sportvision.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SectionRequest(
    @NotBlank(message = "Section name must not be blank")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    String name,

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    String description
) {
}