package com.stockleague.backend.notification.dto;

import java.util.List;

public record NotificationListResponseDto (
        boolean success,
        List<NotificationResponseDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
