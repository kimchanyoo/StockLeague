package com.stockleague.backend.stock.dto.response.watchlist;

import java.util.List;

public record WatchlistListResponseDto(
        boolean success,
        List<WatchlistSummaryDto> watchlists,
        int page,
        int size,
        long totalCount
) {
}
