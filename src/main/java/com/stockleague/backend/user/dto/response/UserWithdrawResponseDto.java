package com.stockleague.backend.user.dto.response;

public record UserWithdrawResponseDto(
        boolean success,
        String message
) {
}
