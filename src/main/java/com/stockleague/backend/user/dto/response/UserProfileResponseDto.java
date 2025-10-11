package com.stockleague.backend.user.dto.response;

public record UserProfileResponseDto(
        boolean success,
        String message,
        String nickname,
        String role
) {
}
