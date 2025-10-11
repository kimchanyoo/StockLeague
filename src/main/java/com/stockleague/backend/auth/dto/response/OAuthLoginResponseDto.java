package com.stockleague.backend.auth.dto.response;

public record OAuthLoginResponseDto (
        boolean success,
        String message,
        boolean isFirstLogin,
        String accessToken,
        String nickname,
        String role
) {
}
