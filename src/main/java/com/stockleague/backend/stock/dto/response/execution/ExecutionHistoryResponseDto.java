package com.stockleague.backend.stock.dto.response.execution;

import java.util.List;

public record ExecutionHistoryResponseDto(
        boolean success,
        List<OrderExecutionSummaryDto> contents,
        int page,
        int size,
        long totalCount,
        long totalPage
) {
}
