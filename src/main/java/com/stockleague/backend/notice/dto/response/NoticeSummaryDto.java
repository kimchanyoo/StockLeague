package com.stockleague.backend.notice.dto.response;

import com.stockleague.backend.notice.domain.Notice;

public record NoticeSummaryDto(
        Long noticeId,
        String title,
        String category,
        boolean isPinned,
        String createdAt
) {
    public static NoticeSummaryDto from(Notice notice) {
        return new NoticeSummaryDto(
                notice.getId(),
                notice.getTitle(),
                notice.getCategory(),
                notice.getIsPinned(),
                notice.getCreatedAt().toString()
        );
    }
}
