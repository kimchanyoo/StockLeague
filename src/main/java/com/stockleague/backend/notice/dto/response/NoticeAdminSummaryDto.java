package com.stockleague.backend.notice.dto.response;

import com.stockleague.backend.notice.domain.Notice;

public record NoticeAdminSummaryDto(
        Long noticeId,
        String title,
        String category,
        boolean isPinned,
        boolean isDeleted,
        String createdAt
) {
    public static NoticeAdminSummaryDto from(Notice notice) {
        return new NoticeAdminSummaryDto(
                notice.getId(),
                notice.getTitle(),
                notice.getCategory(),
                notice.getIsPinned(),
                notice.getDeletedAt() != null,
                notice.getCreatedAt().toString()
        );
    }
}
