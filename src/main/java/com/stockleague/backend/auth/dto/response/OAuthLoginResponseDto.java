package com.stockleague.backend.auth.dto.response;

public record OAuthLoginResponseDto (
        boolean success,
        String message,
        boolean isFirstLogin,
        String tempAccessToken,
        String nickname,
        String role
) {
}
