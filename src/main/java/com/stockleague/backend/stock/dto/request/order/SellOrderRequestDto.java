package com.stockleague.backend.stock.dto.request.order;

import java.math.BigDecimal;

public record SellOrderRequestDto(
        String ticker,
        BigDecimal orderPrice,
        BigDecimal orderAmount
) {
}
