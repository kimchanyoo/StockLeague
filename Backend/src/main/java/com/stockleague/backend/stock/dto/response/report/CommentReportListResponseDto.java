package com.stockleague.backend.stock.dto.response.report;

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
