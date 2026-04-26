package com.github.deniskoriavets.sportvision.service.interfaces;

import com.github.deniskoriavets.sportvision.dto.NotificationMessage;
import com.github.deniskoriavets.sportvision.entity.enums.NotificationPreference;

public interface NotificationStrategy {
    boolean supports(NotificationPreference preference);

    void send(NotificationMessage message);
}
