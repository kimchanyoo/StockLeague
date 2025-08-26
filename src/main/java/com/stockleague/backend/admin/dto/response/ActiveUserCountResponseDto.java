package com.stockleague.backend.admin.dto.response;

public record ActiveUserCountResponseDto(
        boolean success,
        long userCount
) {
}
