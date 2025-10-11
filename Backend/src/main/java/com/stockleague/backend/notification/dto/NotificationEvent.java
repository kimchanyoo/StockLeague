package com.stockleague.backend.notification.dto;

import com.stockleague.backend.notification.domain.NotificationType;
import com.stockleague.backend.notification.domain.TargetType;

public record NotificationEvent(
        Long userId,
        NotificationType type,
        TargetType target,
        Long targetId
) {
    public String resolvedMessage() {
        return type.getDefaultMessage();
    }
}
