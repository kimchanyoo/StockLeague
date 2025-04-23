package com.stockleague.backend.notice.dto.response;

import java.util.List;

public record NoticePageResponseDto(
        boolean success,
        List<NoticeSummaryDto> notices,
        int page,
        int size,
        long totalCount
) {
}
