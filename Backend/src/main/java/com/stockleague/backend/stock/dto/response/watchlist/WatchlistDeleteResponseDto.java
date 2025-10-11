package com.stockleague.backend.stock.dto.response.watchlist;

public record WatchlistDeleteResponseDto(
        boolean success,
        String message
) {
    public static WatchlistDeleteResponseDto from() {
        return new WatchlistDeleteResponseDto(
                true,
                "관심 항목에서 삭제되었습니다."
        );
    }
}
