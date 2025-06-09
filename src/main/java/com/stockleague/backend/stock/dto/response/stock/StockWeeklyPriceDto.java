package com.stockleague.backend.stock.dto.response.stock;

public record StockWeeklyPriceDto(
        String ticker,
        int year,
        int week,
        Long openPrice,
        Long highPrice,
        Long lowPrice,
        Long closePrice,
        Long volume
) {
}
