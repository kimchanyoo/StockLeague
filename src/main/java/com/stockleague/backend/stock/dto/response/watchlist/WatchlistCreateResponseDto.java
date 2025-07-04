package com.stockleague.backend.stock.dto.response.watchlist;

import com.stockleague.backend.stock.domain.Watchlist;

public record WatchlistCreateResponseDto(
        boolean success,
        String message,
        String ticker
) {
    public static WatchlistCreateResponseDto from(Watchlist watchlist) {
        return new WatchlistCreateResponseDto(true,
                "관심 항목에 등록되었습니다.",
                watchlist.getStock().getStockTicker());
    }
}
