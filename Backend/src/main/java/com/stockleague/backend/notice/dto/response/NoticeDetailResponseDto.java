package com.stockleague.backend.notice.dto.response;

import com.stockleague.backend.notice.domain.Notice;
import java.time.format.DateTimeFormatter;

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
                notice.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                notice.getIsPinned()
        );
    }
}
