package com.stockleague.backend.stock.dto.response.watchlist;

import com.stockleague.backend.stock.domain.Watchlist;

public record WatchlistSummaryDto(
        Long watchlistId,
        Long stockId,
        String StockTicker,
        String StockName
) {
    public static WatchlistSummaryDto from(Watchlist watchlist) {
        return new WatchlistSummaryDto(
                watchlist.getId(),
                watchlist.getStock().getId(),
                watchlist.getStock().getStockTicker(),
                watchlist.getStock().getStockName()
        );
    }
}
