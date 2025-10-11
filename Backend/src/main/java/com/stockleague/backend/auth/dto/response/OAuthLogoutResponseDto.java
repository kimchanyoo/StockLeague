package com.stockleague.backend.auth.dto.response;

public record OAuthLogoutResponseDto(
        boolean success,
        String message
) {
}
