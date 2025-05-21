package com.stockleague.backend.stock.dto.request;

import com.stockleague.backend.stock.domain.Status;

public record CommentReportListRequestDto(
        Status status
) {
}
