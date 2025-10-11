package com.stockleague.backend.notice.dto.response;

public record NoticeRestoreResponseDto(
        Boolean success,
        String message,
        Boolean isDeleted
) {
}
