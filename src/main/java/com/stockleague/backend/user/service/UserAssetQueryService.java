package com.stockleague.backend.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.user.dto.response.UserAssetSnapshotDto;
import com.stockleague.backend.user.dto.response.UserAssetValuationDto;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAssetQueryService {

    private final UserAssetService userAssetService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String REDIS_KEY_PREFIX = "user:asset:closing:";

    /**
     * 현재 시각을 기준으로 실시간 자산 평가 또는 장 마감 기준 스냅샷 자산 평가를 반환합니다.
     * <p>
     * - 장중(15:30 이전): Redis에 저장된 현재가를 기반으로 실시간 평가 값을 반환합니다. <br> - 장 마감 이후(15:30 이후): Redis에 저장된 고정된 스냅샷이 있다면 해당 데이터를
     * 반환합니다. <br> - 스냅샷이 없을 경우 fallback으로 실시간 평가 결과를 반환합니다.
     * </p>
     *
     * @param userId 사용자 ID
     * @return 자산 평가 정보 DTO {@link UserAssetValuationDto}
     */
    public UserAssetValuationDto getUserAssetValuation(Long userId) {
        if (isMarketClosed()) {
            UserAssetValuationDto snapshot = getSnapshotFromRedis(userId);
            if (snapshot != null) {
                return snapshot;
            }
        }

        return userAssetService.getLiveAssetValuation(userId);
    }

    /**
     * 현재 시각이 장 마감 이후인지 여부를 반환합니다.
     *
     * @return true: 15시 30분 이후인 경우 (장 마감 이후), false: 장중
     */
    private boolean isMarketClosed() {
        LocalTime now = LocalTime.now();
        return now.isAfter(LocalTime.of(15, 30));
    }

    /**
     * Redis에 저장된 장 마감 시점의 자산 스냅샷을 조회합니다.
     * <p>
     * Redis 키 형식: {@code user:asset:closing:yyyy-MM-dd:{userId}}
     * </p>
     *
     * @param userId 사용자 ID
     * @return Redis에 저장된 스냅샷 정보가 있을 경우 {@link UserAssetValuationDto} 반환, 없을 경우 null 반환
     * @throws GlobalException JSON 역직렬화 실패 시 {@code REDIS_DESERIALIZE_ERROR} 예외 발생
     */
    private UserAssetValuationDto getSnapshotFromRedis(Long userId) {
        String dateKey = LocalDate.now().toString();
        String redisKey = REDIS_KEY_PREFIX + dateKey + ":" + userId;

        try {
            String json = redisTemplate.opsForValue().get(redisKey);
            if (json == null) {
                log.warn("[자산조회] Redis 스냅샷 없음 - key={}", redisKey);
                return null;
            }

            UserAssetSnapshotDto snapshot = objectMapper.readValue(json, UserAssetSnapshotDto.class);

            return UserAssetValuationDto.builder()
                    .cashBalance(snapshot.getCashBalance())
                    .stockValuation(snapshot.getStockValuation())
                    .totalAsset(snapshot.getTotalAsset())
                    .totalProfit(snapshot.getTotalProfit())
                    .totalProfitRate(snapshot.getTotalProfitRate())
                    .stocks(snapshot.getStocks())
                    .build();

        } catch (Exception e) {
            throw new GlobalException(GlobalErrorCode.REDIS_DESERIALIZE_ERROR);
        }
    }
}
