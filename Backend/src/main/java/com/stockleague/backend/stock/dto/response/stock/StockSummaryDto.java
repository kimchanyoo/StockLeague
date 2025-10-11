package com.stockleague.backend.stock.dto.response.stock;

import com.stockleague.backend.stock.domain.MarketType;
import com.stockleague.backend.stock.domain.Stock;

public record StockSummaryDto(
        Long stockId,
        String stockTicker,
        String stockName,
        MarketType marketType
) {
    public static StockSummaryDto from(Stock stock) {
        return new StockSummaryDto(
                stock.getId(),
                stock.getStockTicker(),
                stock.getStockName(),
                stock.getMarketType()
        );
    }
}
