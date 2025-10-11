package com.stockleague.backend.stock.dto.request.order;

import java.math.BigDecimal;

public record BuyOrderRequestDto(
        String ticker,
        BigDecimal orderPrice,
        BigDecimal orderAmount
) {
}
