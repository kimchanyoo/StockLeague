package com.stockleague.backend.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stockleague.backend.notification.domain.Notification;
import java.time.format.DateTimeFormatter;

public record NotificationResponseDto(
        @JsonProperty("notificationId") Long notificationId,
        @JsonProperty("type") String type,
        @JsonProperty("message") String message,
        @JsonProperty("target") String target,
        @JsonProperty("targetId") Long targetId,
        @JsonProperty("isRead") boolean isRead,
        @JsonProperty("createdAt") String createdAt
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
