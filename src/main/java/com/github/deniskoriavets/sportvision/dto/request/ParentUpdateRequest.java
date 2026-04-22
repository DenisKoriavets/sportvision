package com.github.deniskoriavets.sportvision.dto.request;

import com.github.deniskoriavets.sportvision.entity.enums.NotificationPreference;
import com.github.deniskoriavets.sportvision.validation.ValidPhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record ParentUpdateRequest(
    @NotBlank(message = "First name must not be blank") String firstName,
    @NotBlank(message = "Last name must not be blank") String lastName,
    @ValidPhoneNumber String phone,
    @NotEmpty Set<NotificationPreference> notificationPreferences
) {
}