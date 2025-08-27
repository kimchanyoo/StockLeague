package com.stockleague.backend.user.scheduler;

import com.stockleague.backend.global.util.MarketTimeUtil;
import com.stockleague.backend.infra.webSocket.RankingWebSocketPublisher;
import com.stockleague.backend.user.domain.RankingSort;
import com.stockleague.backend.user.dto.response.UserProfitRateRankingDto;
import com.stockleague.backend.user.dto.response.UserProfitRateRankingListDto;
import com.stockleague.backend.user.repository.UserRepository;
import com.stockleague.backend.user.service.UserAssetService;
import com.stockleague.backend.user.service.UserRankingService;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingLivePublisherScheduler {

    private final UserRepository userRepository;
    private final UserAssetService userAssetService;
    private final UserRankingService userRankingService;
    private final RankingWebSocketPublisher publisher;
    private final SimpUserRegistry simpUserRegistry;

    /**
     * 실시간 랭킹(수익률/총자산 기준)을 10초마다 계산하여 WebSocket으로 송신합니다.
     * <p>
     * 실행 조건:
     * - 장이 열려 있을 때만 실행됩니다. (장 마감 시 실행하지 않음)
     *
     * 처리 흐름:
     * 1. UserRankingService를 통해 전체 사용자 자산 평가 정보를 가져옵니다. (수익률/총자산 포함)
     * 2. 수익률 기준 정렬 → 순위 부여 → 전체 브로드캐스트(/topic/ranking/profit)
     * 3. 총자산 기준 정렬 → 순위 부여 → 전체 브로드캐스트(/topic/ranking/asset)
     * 4. 접속 중인 각 사용자별로 개인 순위 정보를 전송
     *    - 수익률 기준: /user/{userId}/queue/ranking/me
     *    - 총자산 기준: /user/{userId}/queue/ranking/me/asset
     *
     * 주의사항:
     * - 보유 종목이 없는 유저도 원금=0으로 간주하여 수익률 0%로 포함되도록
     *   UserRankingService.calculateProfitRate()가 0%를 반환해야 합니다.
     * - WebSocket 구독자는 수익률/총자산 랭킹을 원하는 토픽을 구독하면 됩니다.
     */
    @Scheduled(fixedRate = 10_000)
    public void pushLiveInvestedRanking() {
        if (MarketTimeUtil.isMarketClosed()) return;

        UserProfitRateRankingListDto base =
                userRankingService.getRanking(/*myUserId*/ null, RankingSort.PROFIT_RATE_DESC);

        List<UserProfitRateRankingDto> items = base.rankingList();
        if (items == null || items.isEmpty()) return;

        List<UserProfitRateRankingDto> byProfit = IntStream.range(0, items.size())
                .mapToObj(items::get)
                .sorted(Comparator.comparing(UserProfitRateRankingDto::profitRate, Comparator.reverseOrder())
                        .thenComparing(UserProfitRateRankingDto::totalAsset, Comparator.reverseOrder())
                        .thenComparing(UserProfitRateRankingDto::nickname, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
        byProfit = rankify(byProfit);
        publisher.publishAllByProfit(byProfit, /*marketOpen*/ true);

        List<UserProfitRateRankingDto> byAsset = IntStream.range(0, items.size())
                .mapToObj(items::get)
                .sorted(Comparator.comparing(UserProfitRateRankingDto::totalAsset, Comparator.reverseOrder())
                        .thenComparing(UserProfitRateRankingDto::profitRate, Comparator.reverseOrder())
                        .thenComparing(UserProfitRateRankingDto::nickname, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
        byAsset = rankify(byAsset);
        publisher.publishAllByAsset(byAsset, /*marketOpen*/ true);

        Map<Long, UserProfitRateRankingDto> profitByUser = byProfit.stream()
                .collect(Collectors.toMap(UserProfitRateRankingDto::userId,
                        Function.identity(), (a, b) -> a));
        Map<Long, UserProfitRateRankingDto> assetByUser = byAsset.stream()
                .collect(Collectors.toMap(UserProfitRateRankingDto::userId,
                        Function.identity(), (a, b) -> a));

        for (SimpUser su : simpUserRegistry.getUsers()) {
            Long userId;
            try {
                userId = Long.valueOf(su.getName());
            } catch (Exception ignore) {
                continue;
            }
            publisher.publishMyRankingByProfit(su.getName(), profitByUser.get(userId), true);
            publisher.publishMyAssetRanking(su.getName(), assetByUser.get(userId), true);
        }
    }

    /**
     * 랭킹 리스트에 순위(ranking)를 부여합니다.
     * <p>
     * - 전달받은 리스트는 이미 정렬되어 있어야 합니다.
     * - 순위는 1부터 시작하며, 리스트 순서대로 부여됩니다.
     * </p>
     *
     * @param items 정렬된 랭킹 리스트
     * @return 순위가 부여된 새 리스트
     */
    private List<UserProfitRateRankingDto> rankify(List<UserProfitRateRankingDto> items) {
        return IntStream.range(0, items.size())
                .mapToObj(i -> {
                    var d = items.get(i);
                    return new UserProfitRateRankingDto(
                            d.userId(), d.nickname(), d.profitRate(), d.totalAsset(), i + 1
                    );
                })
                .toList();
    }
}
