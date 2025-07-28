package com.stockleague.backend.user.dto.response;

import java.util.List;

public record UserProfitRateRankingMessage(
        List<UserProfitRateRankingDto> rankingList,
        UserProfitRateRankingDto myRanking
) {
    public static UserProfitRateRankingMessage from(
            List<UserProfitRateRankingDto> list, UserProfitRateRankingDto mine) {
        return new UserProfitRateRankingMessage(
                list,
                mine);
    }
}
