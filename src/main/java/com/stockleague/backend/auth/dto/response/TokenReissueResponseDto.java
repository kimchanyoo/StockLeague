package com.stockleague.backend.auth.dto.response;

public record TokenReissueResponseDto(
        boolean success,
        String message
) {
}
