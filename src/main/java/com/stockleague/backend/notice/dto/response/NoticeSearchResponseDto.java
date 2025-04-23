package com.stockleague.backend.notice.dto.response;

import java.util.List;

public record NoticeSearchResponseDto(
        boolean success,
        List<NoticeSummaryDto> notices,
        int page,
        int size,
        long totalCount
) {
}
