package com.stockleague.backend.stock.dto.response;

import java.util.List;

public record ReplyListResponseDto(
        boolean success,
        List<ReplySummaryDto> data
) {
    public static ReplyListResponseDto from(List<ReplySummaryDto> replies) {
        return new ReplyListResponseDto(true, replies);
    }
}
