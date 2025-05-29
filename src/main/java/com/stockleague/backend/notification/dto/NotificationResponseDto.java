package com.stockleague.backend.notification.dto;

import com.stockleague.backend.notification.domain.Notification;
import java.time.format.DateTimeFormatter;

public record NotificationResponseDto(
        Long notificationId,
        String type,
        String message,
        String target,
        Long targetId,
        boolean isRead,
        String createdAt
) {
    public static NotificationResponseDto from(Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getType().name(),
                notification.getMessage(),
                notification.getTarget().name(),
                notification.getTargetId(),
                notification.isRead(),
                notification.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
