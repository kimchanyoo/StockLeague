package com.stockleague.backend.user.dto.response;

public record UserProfitRateRankingDto(
        Long userId,
        String nickname,
        String profitRate,
        String totalAsset,
        Integer ranking
) {
}
