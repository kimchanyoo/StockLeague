package com.stockleague.backend.user.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record UserProfitRateRankingListDto(
        List<UserProfitRateRankingDto> rankingList,
        UserProfitRateRankingDto myRanking,
        long totalCount,
        boolean isMarketOpen,
        LocalDateTime generatedAt
) {
}
