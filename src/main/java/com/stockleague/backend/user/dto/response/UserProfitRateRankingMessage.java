package com.stockleague.backend.user.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record UserProfitRateRankingMessage(
        List<UserProfitRateRankingDto> rankingList,
        UserProfitRateRankingDto myRanking,
        long totalCount,
        boolean marketOpen,
        LocalDateTime generatedAt
) {
    /** 브로드캐스트 메시지 생성 (/topic/ranking) */
    public static UserProfitRateRankingMessage broadcast(
            List<UserProfitRateRankingDto> list,
            boolean marketOpen
    ) {
        return new UserProfitRateRankingMessage(
                list,
                null,
                list == null ? 0 : list.size(),
                marketOpen,
                LocalDateTime.now()
        );
    }

    /** 개인용 메시지 생성 (/user/queue/ranking/me) */
    public static UserProfitRateRankingMessage personal(
            UserProfitRateRankingDto mine,
            boolean marketOpen
    ) {
        return new UserProfitRateRankingMessage(
                null,
                mine,
                0,
                marketOpen,
                LocalDateTime.now()
        );
    }
}
