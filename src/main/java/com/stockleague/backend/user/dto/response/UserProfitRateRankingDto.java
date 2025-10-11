package com.stockleague.backend.user.dto.response;

import java.math.BigDecimal;

public record UserProfitRateRankingDto(
        Long userId,
        String nickname,
        BigDecimal profitRate,
        BigDecimal totalAsset,
        Integer ranking
) {
}
