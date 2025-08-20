package com.stockleague.backend.infra.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockleague.backend.global.util.MarketTimeUtil;
import com.stockleague.backend.stock.dto.response.stock.StockOrderBookDto;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockOrderBookRedisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper mapper;

    private static final String LIVE_PREFIX = "stock:orderbook:";
    private static final String LAST_PREFIX = "stock:orderbook:last:";
    private static final Duration LIVE_TTL = Duration.ofSeconds(3);

    /**
     * 호가 저장
     * - 15:00 이전(평일 장중)만 저장 허용
     * - 저장 시 LIVE(3s TTL) + LAST(영구) 동시 갱신
     * - 15:00 이후에는 저장 차단
     */
    public void save(StockOrderBookDto dto) {
        try {
            if (!MarketTimeUtil.shouldCollectOrderbookNow()) {
                log.debug("[Redis] 15:00 이후 호가 저장 차단: {}", dto != null ? dto.ticker() : "null");
                return;
            }
            if (dto == null) {
                log.debug("[Redis] null 호가 → 저장 스킵");
                return;
            }

            final String liveKey = getLiveKey(dto.ticker());
            final String lastKey = getLastKey(dto.ticker());
            final String json = mapper.writeValueAsString(dto);

            redisTemplate.opsForValue().set(liveKey, json, LIVE_TTL);
            redisTemplate.opsForValue().set(lastKey, json);

        } catch (JsonProcessingException e) {
            log.error("[Redis] 호가 저장 실패 - {}", e.getMessage(), e);
        }
    }

    /**
     * 조회
     * - 15:00 이전: LIVE에서
     * - 15:00 이후: LAST 스냅샷에서 (LIVE는 TTL로 사라질 수 있음)
     */
    public StockOrderBookDto get(String ticker) {
        return MarketTimeUtil.shouldCollectOrderbookNow()
                ? getLive(ticker)
                : getLastSnapshot(ticker);
    }

    /** LIVE(3초 TTL) 호가 조회 */
    public StockOrderBookDto getLive(String ticker) {
        try {
            String key = getLiveKey(ticker);
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) return null;
            return mapper.readValue(json, StockOrderBookDto.class);
        } catch (Exception e) {
            log.error("[Redis] LIVE 호가 조회 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    /** LAST(영구) 마지막 스냅샷 호가 조회 */
    public StockOrderBookDto getLastSnapshot(String ticker) {
        try {
            String key = getLastKey(ticker);
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) return null;
            return mapper.readValue(json, StockOrderBookDto.class);
        } catch (Exception e) {
            log.error("[Redis] LAST 스냅샷 조회 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    private String getLiveKey(String ticker) {
        return LIVE_PREFIX + ticker;
    }

    private String getLastKey(String ticker) {
        return LAST_PREFIX + ticker;
    }
}
