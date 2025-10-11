package com.stockleague.backend.admin.dto.response;

public record NewUserCountResponseDto(
        boolean success,
        long userCount
) {
}
