package com.stockleague.backend.stock.dto.response.order;

import java.util.List;

public record OrderListResponseDto(
        boolean success,
        List<OrderSummaryDto> orders,
        int page,
        int size,
        long totalCount,
        long totalPages
) {
}
