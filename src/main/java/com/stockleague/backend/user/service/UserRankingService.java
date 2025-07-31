package com.stockleague.backend.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockleague.backend.global.util.MarketTimeUtil;
import com.stockleague.backend.user.dto.projection.UserIdAndNicknameProjection;
import com.stockleague.backend.user.dto.response.UserAssetSnapshotDto;
import com.stockleague.backend.user.dto.response.UserAssetValuationDto;
import com.stockleague.backend.user.dto.response.UserProfitRateRankingDto;
import com.stockleague.backend.user.dto.response.UserProfitRateRankingListResponseDto;
import com.stockleague.backend.user.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
     * 현재 장 상태에 따라 적절한 수익률 랭킹 조회 API를 호출합니다.
     * <p>
     * - 장이 열려 있으면 실시간 랭킹({@link #getLiveRanking(Long)})을 조회하고,<br>
     * - 장이 닫혀 있으면 Redis에 저장된 장 마감 기준
     * 랭킹({@link #getRankingFromSnapshot(Long)})을 조회합니다.
     * </p>
     *
     * @param myUserId 현재 로그인한 유저 ID
     * @return 전체 랭킹 리스트 및 나의 순위가 포함된 응답 DTO
     */
    public UserProfitRateRankingListResponseDto getProfitRateRanking(Long myUserId) {
        if (MarketTimeUtil.isMarketClosed()) {
            return getRankingFromSnapshot(myUserId);
        } else {
            return getLiveRanking(myUserId);
        }
    }

    /**
     * 장 마감 후 Redis에 저장된 스냅샷을 기반으로 수익률 랭킹을 조회합니다.
     * <p>
     * - Redis 키: user:asset:closing:yyyy-MM-dd:{userId} 형식<br>
     * - 수익률은 스냅샷에 저장된 값을 사용하고, 유저 닉네임은 projection으로 조회합니다.<br>
     * - 스냅샷이 없거나 수익률이 null인 경우 해당 유저는 제외됩니다.
     * </p>
     *
     * @param myUserId 현재 유저 ID (나의 순위를 계산하기 위함)
     * @return 전체 유저 수익률 랭킹 및 나의 랭킹 포함 DTO
     */
    private UserProfitRateRankingListResponseDto getRankingFromSnapshot(Long myUserId) {
        List<Long> userIds = userRepository.findAllUserIds();
        String snapshotPrefix = REDIS_SNAPSHOT_PREFIX + LocalDate.now() + ":";

        if (userIds.isEmpty()) {
            return new UserProfitRateRankingListResponseDto(
                    List.of(), null, 0, true);
        }

        List<UserIdAndNicknameProjection> projections = userRepository.findIdAndNicknameByIds(userIds);
        Map<Long, String> nicknameMap = projections.stream()
                .collect(Collectors.toMap(UserIdAndNicknameProjection::getId, UserIdAndNicknameProjection::getNickname));

        List<UserProfitRateRankingDto> rankings = new ArrayList<>();

        for (Long userId : userIds) {
            try {
                String json = stringRedisTemplate.opsForValue().get(snapshotPrefix + userId);
                if (json == null) {
                    continue;
                }

                UserAssetSnapshotDto snapshot = objectMapper.readValue(json, UserAssetSnapshotDto.class);
                BigDecimal profitRate = snapshot.getTotalProfitRate();
                if (profitRate == null) {
                    continue;
                }

                String nickname = nicknameMap.getOrDefault(userId, "탈퇴회원");

                rankings.add(new UserProfitRateRankingDto(
                        userId,
                        nickname,
                        profitRate.setScale(2, RoundingMode.HALF_UP).toString(),
                        snapshot.getTotalAsset().toString(),
                        null
                ));
            } catch (Exception e) {
                log.warn("[스냅샷 랭킹 계산 실패] userId={}, err={}", userId, e.getMessage());
            }
        }

        return sortAndWrap(rankings, myUserId);
    }

    /**
     * 현재 자산 정보를 기반으로 실시간 수익률 랭킹을 계산합니다.
     * <p>
     * - 모든 유저의 실시간 자산 정보를 조회하여 평균 매수가, 보유 수량, 현재가로 수익률을 계산합니다.<br>
     * - 계산 도중 예외 발생 시 해당 유저는 제외됩니다.
     * </p>
     *
     * @param myUserId 현재 유저 ID
     * @return 전체 유저 수익률 랭킹 및 나의 랭킹 포함 DTO
     */
    private UserProfitRateRankingListResponseDto getLiveRanking(Long myUserId) {
        List<Long> userIds = userRepository.findAllUserIds();
        if (userIds.isEmpty()) {
            return new UserProfitRateRankingListResponseDto(
                    List.of(), null, 0, false);
        }

        List<UserIdAndNicknameProjection> users = userRepository.findIdAndNicknameByIds(userIds);
        List<UserProfitRateRankingDto> rankings = new ArrayList<>();

        for (UserIdAndNicknameProjection user : users) {
            try {
                Long userId = user.getId();
                String nickname = user.getNickname();

                UserAssetValuationDto valuation = userAssetService.getLiveAssetValuation(userId, true);
                BigDecimal profitRate = calculateProfitRate(valuation);
                if (profitRate == null) {
                    continue;
                }

                rankings.add(new UserProfitRateRankingDto(
                        userId,
                        nickname,
                        profitRate.toString(),
                        valuation.getTotalAsset().setScale(0, RoundingMode.HALF_UP).toString(),
                        null
                ));
            } catch (Exception e) {
                log.warn("[실시간 수익률 계산 실패] userId={}, err={}", user.getId(), e.getMessage());
            }
        }

        return sortAndWrap(rankings, myUserId);
    }

    /**
     * 수익률을 기준으로 정렬하고 순위를 부여하여 응답 형식으로 반환합니다.
     * <p>
     * - 수익률 기준 내림차순 정렬<br>
     * - 랭킹은 1부터 시작<br>
     * - 내 랭킹을 별도로 추출하여 포함
     * </p>
     *
     * @param list     수익률 DTO 리스트
     * @param myUserId 현재 유저 ID
     * @return 정렬 및 랭킹 부여된 응답 DTO
     */
    private UserProfitRateRankingListResponseDto sortAndWrap(List<UserProfitRateRankingDto> list, Long myUserId) {
        list.sort(Comparator.comparing(
                dto -> new BigDecimal(dto.profitRate()), Comparator.reverseOrder()));

        for (int i = 0; i < list.size(); i++) {
            UserProfitRateRankingDto dto = list.get(i);
            list.set(i, new UserProfitRateRankingDto(
                    dto.userId(), dto.nickname(), dto.profitRate(), dto.totalAsset(), i + 1));
        }

        UserProfitRateRankingDto myRanking = list.stream()
                .filter(dto -> dto.userId().equals(myUserId))
                .findFirst()
                .orElse(null);

        return new UserProfitRateRankingListResponseDto(list, myRanking, list.size(), MarketTimeUtil.isMarketClosed());
    }

    /**
     * 자산 정보를 기반으로 수익률을 계산합니다.
     * <p>
     * - 총 매수 원금 = ∑(평균매수가 × 보유 수량)<br>
     * - 총 평가 금액 = ∑(현재가 × 보유 수량)<br>
     * - 수익률 = (평가금액 - 원금) / 원금 * 100 (% 단위)
     * </p>
     *
     * @param valuation 사용자 자산 평가 정보
     * @return 수익률 (%), 계산 불가능하면 null
     */
    public BigDecimal calculateProfitRate(UserAssetValuationDto valuation) {
        BigDecimal totalCost = valuation.getStocks().stream()
                .map(s -> s.getAvgBuyPrice().multiply(s.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalCost.compareTo(BigDecimal.ZERO) == 0) {
            return null;
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
