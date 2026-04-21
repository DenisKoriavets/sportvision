package com.github.deniskoriavets.sportvision.dto.response;

import java.util.UUID;

public record SectionResponse(
    UUID id,
    String name,
    String description
) {
}