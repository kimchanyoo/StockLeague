package com.stockleague.backend.user.dto.response;

import java.time.LocalDateTime;

public record UserProfileUpdateResponseDto(
        boolean success,
        String message,
        String nickname,
        LocalDateTime lastNicknameChangedAt,
        LocalDateTime nextNicknameChangeAt
) {
}
