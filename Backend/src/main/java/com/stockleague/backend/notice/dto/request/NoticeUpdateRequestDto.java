package com.stockleague.backend.notice.dto.request;

public record NoticeUpdateRequestDto(
        String title,
        String category,
        String content,
        Boolean isPinned
) {
}
