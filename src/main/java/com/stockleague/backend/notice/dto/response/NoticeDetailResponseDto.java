package com.stockleague.backend.notice.dto.response;

import com.stockleague.backend.notice.domain.Notice;

public record NoticeDetailResponseDto(
        boolean success,
        Long noticeId,
        String title,
        String category,
        String content,
        String createdAt,
        boolean isPinned
) {
    public static NoticeDetailResponseDto from(Notice notice) {
        return new NoticeDetailResponseDto(
                true,
                notice.getId(),
                notice.getTitle(),
                notice.getCategory(),
                notice.getContent(),
                notice.getCreatedAt().toString(),
                notice.getIsPinned()
        );
    }
}
