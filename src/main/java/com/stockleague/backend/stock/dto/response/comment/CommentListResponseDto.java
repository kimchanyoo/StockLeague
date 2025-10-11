package com.stockleague.backend.stock.dto.response.comment;

import java.util.List;

public record CommentListResponseDto(
        boolean success,
        List<CommentSummaryDto> comments,
        int page,
        int size,
        long totalCount
) {
}
