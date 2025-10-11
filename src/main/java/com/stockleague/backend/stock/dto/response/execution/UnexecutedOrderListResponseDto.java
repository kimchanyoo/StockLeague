package com.stockleague.backend.stock.dto.response.execution;

import java.util.List;

public record UnexecutedOrderListResponseDto(
        boolean success,
        List<UnexecutedOrderSummaryDto> contents,
        int page,
        int size,
        long totalCount,
        long totalPage
) {
}
