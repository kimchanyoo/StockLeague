package com.stockleague.backend.stock.dto.response.stock;

public record StockYearlyPriceDto(
        String ticker,
        int year,
        Long openPrice,
        Long highPrice,
        Long lowPrice,
        Long closePrice,
        Long volume
) {
}
