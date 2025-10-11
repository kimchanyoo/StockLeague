package com.stockleague.backend.notice.dto.response;

public record NoticeCreateResponseDto(
        boolean success,
        String message,
        Long noticeId
) {
}
