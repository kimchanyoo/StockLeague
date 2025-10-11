package com.stockleague.backend.stock.dto.request.report;

import com.stockleague.backend.stock.domain.Status;

public record CommentReportListRequestDto(
        Status status
) {
}
