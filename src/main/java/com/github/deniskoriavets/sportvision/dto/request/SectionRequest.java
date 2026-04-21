package com.github.deniskoriavets.sportvision.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SectionRequest(
    @NotBlank(message = "Назва секції не може бути порожньою")
    @Size(max = 255, message = "Назва не може бути довшою за 255 символів")
    String name,

    @Size(max = 1000, message = "Опис не може бути довшим за 1000 символів")
    String description
) {
}