package com.stockleague.backend.user.dto.response;

import java.util.List;

public record UserProfitRateRankingListResponseDto(
        List<UserProfitRateRankingDto> rankingList,
        UserProfitRateRankingDto myRanking,
        long totalCount,
        boolean isMarketOpen
) {
}
