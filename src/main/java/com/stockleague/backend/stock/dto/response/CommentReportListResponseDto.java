package com.stockleague.backend.stock.dto.response;

import java.util.List;

public record CommentReportListResponseDto(
        boolean success,
        List<CommentReportSummaryDto> reports,
        int page,
        int size,
        long totalCount,
        long totalPage
) {
}
