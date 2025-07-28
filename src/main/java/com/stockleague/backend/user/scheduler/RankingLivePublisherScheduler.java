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
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * 10초마다 전체 유저의 실시간 수익률 랭킹을 계산하여 WebSocket으로 전송합니다.
     *
     * <p>
     * - 장이 열려 있을 때만 실행됩니다. <br>
     * - 각 유저의 실시간 자산 정보({@link UserAssetValuationDto})를 조회하여, <br>
     *   평균 매수가(avgBuyPrice), 현재가(currentPrice), 보유 수량(quantity)를 바탕으로
     *   <b>총 투자 원금</b>과 <b>총 평가 금액</b>을 계산합니다. <br>
     * - 수익률은 <code>(평가 금액 - 투자 원금) / 투자 원금 * 100</code> 으로 계산되며, 소수점 둘째 자리까지 반올림됩니다. <br>
     * - 계산된 수익률 기준으로 유저들을 정렬하고, 순위를 부여한 뒤 WebSocket으로 전체 랭킹을 브로드캐스트합니다.
     * </p>
     *
     * <p>
     * WebSocket 전송 채널은 {@code /topic/ranking}이며,
     * {@link RankingWebSocketPublisher}를 통해 전달됩니다.
     * </p>
     *
     * 예외 발생 시 개별 유저에 대해 로그만 출력하고 전체 실행은 계속됩니다.
     */
    @Scheduled(fixedRate = 10_000)
    public void pushLiveInvestedRanking() {
        if (MarketTimeUtil.isMarketClosed()) return;

        List<UserIdAndNicknameProjection> users = userRepository.findIdAndNicknameByIds();

        List<UserProfitRateRankingDto> rankingList = new ArrayList<>();

        for (UserIdAndNicknameProjection user : users) {
            try {
                Long userId = user.getId();
                String nickname = user.getNickname();
                UserAssetValuationDto valuation = userAssetService.getLiveAssetValuation(userId, true);

                BigDecimal profitRate = userRankingService.calculateProfitRate(valuation);
                if (profitRate == null) continue;

                BigDecimal totalAsset = valuation.getTotalAsset().setScale(0, RoundingMode.HALF_UP);

                rankingList.add(new UserProfitRateRankingDto(
                        userId,
                        nickname,
                        profitRate.toString(),
                        totalAsset.toString(),
                        null
                ));
            } catch (Exception e) {
                log.warn("실시간 원금 랭킹 계산 실패: userId={}, err={}", user.getId(), e.getMessage());
            }
        }

        List<UserProfitRateRankingDto> sorted = rankingList.stream()
                .sorted(Comparator.comparing((UserProfitRateRankingDto dto) -> new BigDecimal(dto.profitRate())).reversed())
                .map(new Function<UserProfitRateRankingDto, UserProfitRateRankingDto>() {
                    private int rank = 1;

                    @Override
                    public UserProfitRateRankingDto apply(UserProfitRateRankingDto dto) {
                        return new UserProfitRateRankingDto(
                                dto.userId(),
                                dto.nickname(),
                                dto.profitRate(),
                                dto.totalAsset(),
                                rank++
                        );
                    }
                })
                .collect(Collectors.toList());

        UserProfitRateRankingDto myRanking = sorted.stream()
                .findFirst()
                .orElse(null);

        publisher.publish(sorted, myRanking);
    }
}
