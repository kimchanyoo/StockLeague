package com.stockleague.backend.user.scheduler;

import com.stockleague.backend.global.util.MarketTimeUtil;
import com.stockleague.backend.infra.webSocket.RankingWebSocketPublisher;
import com.stockleague.backend.user.dto.projection.UserIdAndNicknameProjection;
import com.stockleague.backend.user.dto.response.UserAssetValuationDto;
import com.stockleague.backend.user.dto.response.UserProfitRateRankingDto;
import com.stockleague.backend.user.repository.UserRepository;
import com.stockleague.backend.user.service.UserAssetService;
import com.stockleague.backend.user.service.UserRankingService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
     * 10초마다 전체 유저의 실시간 수익률 랭킹을 계산하여 WebSocket으로 전송합니다.
     * - 장이 열려 있을 때만 실행
     * - 보유 종목이 없어도 0% 수익률로 포함되도록 calculateProfitRate 수정되어 있어야 함
     */
    @Scheduled(fixedRate = 10_000)
    public void pushLiveInvestedRanking() {
        if (MarketTimeUtil.isMarketClosed()) return;

        List<Long> userIds = userRepository.findAllUserIds();
        if (userIds.isEmpty()) return;

        List<UserIdAndNicknameProjection> users = userRepository.findIdAndNicknameByIds(userIds);

        List<UserProfitRateRankingDto> items = new ArrayList<>(users.size());

        for (UserIdAndNicknameProjection user : users) {
            try {
                Long userId = user.getId();
                String nickname = user.getNickname();

                UserAssetValuationDto valuation = userAssetService.getLiveAssetValuation(userId, true);
                if (valuation == null) continue;

                BigDecimal profitRate = userRankingService.calculateProfitRate(valuation);
                if (profitRate == null) continue; // 원금 0 → 0%로 반환하도록 이미 바꿨다면 사실상 발생 X

                BigDecimal totalAsset = valuation.getTotalAsset().setScale(0, RoundingMode.HALF_UP);

                items.add(new UserProfitRateRankingDto(
                        userId,
                        nickname,
                        profitRate.setScale(2, RoundingMode.HALF_UP),
                        totalAsset,
                        null
                ));
            } catch (Exception e) {
                log.warn("[실시간 수익률 계산 실패] userId={}, err={}", user.getId(), e.getMessage());
            }
        }

        if (items.isEmpty()) return;

        items.sort(
                Comparator.comparing(UserProfitRateRankingDto::profitRate, Comparator.reverseOrder())
                        .thenComparing(UserProfitRateRankingDto::nickname, Comparator.nullsLast(String::compareTo))
        );

        List<UserProfitRateRankingDto> ranked = IntStream.range(0, items.size())
                .mapToObj(i -> {
                    var d = items.get(i);
                    return new UserProfitRateRankingDto(
                            d.userId(), d.nickname(), d.profitRate(), d.totalAsset(), i + 1
                    );
                })
                .toList();

        publisher.publishAll(ranked, /*marketOpen*/ true);

        Map<Long, UserProfitRateRankingDto> byUser = ranked.stream()
                .collect(Collectors.toMap(UserProfitRateRankingDto::userId, Function.identity(), (a,b) -> a));

        for (SimpUser su : simpUserRegistry.getUsers()) {
            Long userId;
            try {
                userId = Long.valueOf(su.getName());
            } catch (Exception ignore) {
                continue;
            }
            UserProfitRateRankingDto my = byUser.get(userId);
            publisher.publishMyRanking(su.getName(), my,true);
        }
    }
}
