package com.github.deniskoriavets.sportvision.dto;

import java.util.UUID;

public record SectionResponse(
    UUID id,
    String name,
    String description
) {
}