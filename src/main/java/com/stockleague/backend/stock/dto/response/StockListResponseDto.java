package com.stockleague.backend.stock.dto.response;

import java.util.List;

public record StockListResponseDto(
        boolean success,
        String message,
        List<StockSummaryDto> stocks
) {
}
