package com.stockleague.backend.user.dto.response;

public record UserProfileUpdateResponseDto(
        boolean success,
        String message,
        String nickname
) {
}
