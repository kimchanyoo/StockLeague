package com.stockleague.backend.stock.dto.request.report;

import com.stockleague.backend.stock.domain.Reason;

public record CommentDeleteAdminRequestDto(
        Reason reason
) {
}
