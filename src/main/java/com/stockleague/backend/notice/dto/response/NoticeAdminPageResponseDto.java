package com.stockleague.backend.notice.dto.response;

import java.util.List;

public record NoticeAdminPageResponseDto (
        boolean success,
        List<NoticeAdminSummaryDto> notices,
        int page,
        int size,
        long totalCount
) {
}
