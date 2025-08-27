package com.stockleague.backend.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockleague.backend.global.util.MarketTimeUtil;
import com.stockleague.backend.user.domain.RankingSort;
import com.stockleague.backend.user.dto.projection.UserIdAndNicknameProjection;
import com.stockleague.backend.user.dto.response.UserAssetSnapshotDto;
import com.stockleague.backend.user.dto.response.UserAssetValuationDto;
import com.stockleague.backend.user.dto.response.UserProfitRateRankingDto;
import com.stockleague.backend.user.dto.response.UserProfitRateRankingListDto;
import com.stockleague.backend.user.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRankingService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final UserAssetService userAssetService;

    private static final String REDIS_SNAPSHOT_PREFIX = "user:asset:closing:";

    /**
     * 현재 장 상태와 정렬 기준에 따라 적절한 랭킹을 조회합니다.
     * <p>
     * - 장중(시장 열림): 실시간 자산 정보를 기반으로 랭킹 계산
     * - 장마감(시장 닫힘): Redis에 저장된 장 마감 스냅샷 정보를 기반으로 랭킹 계산
     * - 정렬 기준: {@link RankingSort} (수익률/총자산)
     * </p>
     *
     * @param myUserId 현재 로그인한 사용자 ID
     * @param sort     정렬 기준 (수익률/총자산)
     * @return 전체 랭킹 및 나의 랭킹 정보가 포함된 DTO
     */
    public UserProfitRateRankingListDto getRanking(Long myUserId, RankingSort sort) {
        if (MarketTimeUtil.isMarketOpen()) {
            return getLiveRanking(myUserId, sort);
        } else {
            return getRankingFromSnapshot(myUserId, sort);
        }
    }

    /**
     * 장 마감 후 Redis 스냅샷 데이터를 기반으로 랭킹을 계산합니다.
     * <p>
     * - Redis 키 형식: {@code user:asset:closing:yyyy-MM-dd:{userId}}
     * - 스냅샷에 저장된 총자산/수익률 값을 사용
     * - 스냅샷이 없거나 총자산이 null이면 해당 유저는 제외
     * - 정렬 기준: {@link RankingSort} (수익률/총자산)
     * </p>
     *
     * @param myUserId 현재 로그인한 사용자 ID
     * @param sort     정렬 기준
     * @return 랭킹 결과 DTO
     */
    private UserProfitRateRankingListDto getRankingFromSnapshot(Long myUserId, RankingSort sort) {
        List<Long> userIds = userRepository.findAllUserIds();

        if (userIds.isEmpty()) {
            return new UserProfitRateRankingListDto(
                    List.of(), null, 0, false, LocalDateTime.now());
        }

        String snapshotPrefix = REDIS_SNAPSHOT_PREFIX + LocalDate.now() + ":";

        List<UserIdAndNicknameProjection> projections = userRepository.findIdAndNicknameByIds(userIds);
        Map<Long, String> nicknameMap = projections.stream()
                .collect(Collectors.toMap(UserIdAndNicknameProjection::getId, UserIdAndNicknameProjection::getNickname));

        List<UserProfitRateRankingDto> rankings = new ArrayList<>();

        for (Long userId : userIds) {
            try {
                String json = stringRedisTemplate.opsForValue().get(snapshotPrefix + userId);
                if (json == null) continue;

                UserAssetSnapshotDto snapshot = objectMapper.readValue(json, UserAssetSnapshotDto.class);
                BigDecimal profitRate = snapshot.getTotalProfitRate();
                BigDecimal totalAsset = snapshot.getTotalAsset();

                if (totalAsset == null) continue;

                String nickname = nicknameMap.getOrDefault(userId, "탈퇴회원");

                rankings.add(new UserProfitRateRankingDto(
                        userId,
                        nickname,
                        profitRate == null ? null : profitRate.setScale(2, RoundingMode.HALF_UP),
                        totalAsset.setScale(0, RoundingMode.HALF_UP),
                        null
                ));
            } catch (Exception e) {
                log.warn("[스냅샷 랭킹 계산 실패] userId={}, err={}", userId, e.getMessage());
            }
        }

        return sortAndWrap(rankings, myUserId, false, sort);
    }

    /**
     * 현재 실시간 자산 정보를 기반으로 랭킹을 계산합니다.
     * <p>
     * - Redis 현재가 기반으로 사용자별 보유 자산을 평가
     * - 총자산, 수익률 계산 후 랭킹 산출
     * - 예외 발생 시 해당 유저는 제외
     * - 정렬 기준: {@link RankingSort} (수익률/총자산)
     * </p>
     *
     * @param myUserId 현재 로그인한 사용자 ID
     * @param sort     정렬 기준
     * @return 랭킹 결과 DTO
     */
    private UserProfitRateRankingListDto getLiveRanking(Long myUserId, RankingSort sort) {
        List<Long> userIds = userRepository.findAllUserIds();
        if (userIds.isEmpty()) {
            return new UserProfitRateRankingListDto(List.of(), null, 0, true, LocalDateTime.now());
        }

        List<UserIdAndNicknameProjection> users = userRepository.findIdAndNicknameByIds(userIds);
        List<UserProfitRateRankingDto> rankings = new ArrayList<>(users.size());

        for (UserIdAndNicknameProjection user : users) {
            try {
                Long userId = user.getId();
                String nickname = user.getNickname();

                UserAssetValuationDto valuation = userAssetService.getLiveAssetValuation(userId, true);
                if (valuation == null) continue;

                BigDecimal totalAsset = valuation.getTotalAsset();
                if (totalAsset == null) continue;

                BigDecimal profitRate = calculateProfitRate(valuation);

                rankings.add(new UserProfitRateRankingDto(
                        userId,
                        nickname,
                        profitRate == null ? null : profitRate.setScale(2, RoundingMode.HALF_UP),
                        totalAsset.setScale(0, RoundingMode.HALF_UP),
                        null
                ));
            } catch (Exception e) {
                log.warn("[실시간 수익률 계산 실패] userId={}, err={}", user.getId(), e.getMessage());
            }
        }

        return sortAndWrap(rankings, myUserId, true, sort);
    }

    /**
     * 랭킹 리스트를 주어진 기준에 따라 정렬하고 순위를 부여합니다.
     * <p>
     * - 수익률 기준 내림차순 정렬: {@link RankingSort#PROFIT_RATE_DESC}
     * - 총자산 기준 내림차순 정렬: {@link RankingSort#TOTAL_ASSET_DESC}
     * - 동률일 경우 보조 정렬 기준: (총자산/수익률 → 닉네임 → userId)
     * - 순위는 1부터 시작하며, 나의 랭킹 정보도 별도로 추출
     * </p>
     *
     * @param list        정렬할 랭킹 DTO 리스트
     * @param myUserId    현재 로그인한 사용자 ID
     * @param isMarketOpen 시장 열림 여부
     * @param sort        정렬 기준
     * @return 정렬 및 순위가 부여된 DTO
     */
    private UserProfitRateRankingListDto sortAndWrap(
            List<UserProfitRateRankingDto> list,
            Long myUserId,
            boolean isMarketOpen,
            RankingSort sort
    ) {
        Comparator<UserProfitRateRankingDto> byProfitDesc =
                Comparator.comparing(UserProfitRateRankingDto::profitRate,
                        Comparator.nullsLast(Comparator.reverseOrder()));

        Comparator<UserProfitRateRankingDto> byAssetDesc =
                Comparator.comparing(UserProfitRateRankingDto::totalAsset,
                        Comparator.nullsLast(Comparator.reverseOrder()));

        Comparator<UserProfitRateRankingDto> tieBreaker =
                Comparator.comparing(UserProfitRateRankingDto::nickname, Comparator.nullsLast(String::compareTo))
                        .thenComparing(UserProfitRateRankingDto::userId, Comparator.nullsLast(Long::compareTo));

        Comparator<UserProfitRateRankingDto> finalComparator =
                (sort == RankingSort.TOTAL_ASSET_DESC)
                        ? byAssetDesc.thenComparing(byProfitDesc).thenComparing(tieBreaker)
                        : byProfitDesc.thenComparing(byAssetDesc).thenComparing(tieBreaker);

        list.sort(finalComparator);

        for (int i = 0; i < list.size(); i++) {
            var d = list.get(i);
            list.set(i, new UserProfitRateRankingDto(
                    d.userId(), d.nickname(), d.profitRate(), d.totalAsset(), i + 1
            ));
        }

        UserProfitRateRankingDto myRanking = (myUserId == null) ? null :
                list.stream()
                        .filter(dto -> Objects.equals(dto.userId(), myUserId))
                        .findFirst()
                        .orElse(null);

        return new UserProfitRateRankingListDto(list, myRanking, list.size(), isMarketOpen, LocalDateTime.now());
    }

    /**
     * 개별 사용자의 자산 정보를 기반으로 수익률(%)을 계산합니다.
     * <p>
     * - 총 매수 원금 = ∑(평균 매수가 × 보유 수량)
     * - 총 평가 금액 = ∑(현재가 × 보유 수량)
     * - 수익률 = (평가금액 - 원금) / 원금 × 100
     * - 원금이 0인 경우 0% 반환
     * </p>
     *
     * @param valuation 사용자 자산 평가 정보
     * @return 수익률(%) 값
     */
    public BigDecimal calculateProfitRate(UserAssetValuationDto valuation) {
        BigDecimal totalCost = valuation.getStocks().stream()
                .map(s -> s.getAvgBuyPrice().multiply(s.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalValuation = valuation.getStocks().stream()
                .map(s -> s.getCurrentPrice().multiply(s.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalValuation.subtract(totalCost)
                .divide(totalCost, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
