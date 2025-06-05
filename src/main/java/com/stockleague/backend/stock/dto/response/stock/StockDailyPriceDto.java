package com.stockleague.backend.stock.dto.response.stock;

import java.time.LocalDate;

public record StockDailyPriceDto(
        String ticker,
        LocalDate date,
        Long openPrice,
        Long highPrice,
        Long lowPrice,
        Long closePrice,
        Long volume
) {
}
