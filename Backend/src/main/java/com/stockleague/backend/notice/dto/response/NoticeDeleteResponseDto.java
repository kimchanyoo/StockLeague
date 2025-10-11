package com.stockleague.backend.notice.dto.response;

public record NoticeDeleteResponseDto(
        Boolean success,
        String message,
        String deletedAt
) {
}
