package com.stockleague.backend.admin.dto.response;

public record HealthSimpleResponseDto(
        boolean success,
        boolean healthy
) {
}
