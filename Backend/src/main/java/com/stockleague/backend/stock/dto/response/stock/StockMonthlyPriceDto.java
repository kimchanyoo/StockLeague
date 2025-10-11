package com.stockleague.backend.stock.dto.response.stock;

public record StockMonthlyPriceDto(
        String ticker,
        int year,
        int month,
        Long openPrice,
        Long highPrice,
        Long lowPrice,
        Long closePrice,
        Long volume
) {
}
