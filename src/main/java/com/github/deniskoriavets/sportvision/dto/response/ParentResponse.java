package com.github.deniskoriavets.sportvision.dto.response;

import com.github.deniskoriavets.sportvision.entity.enums.Role;
import java.util.UUID;

public record ParentResponse(
    UUID id,
    String firstName,
    String lastName,
    String email,
    String phone,
    Role role
) {
}
