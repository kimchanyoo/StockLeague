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
     * 호가 정보를 Redis에 저장
     * - 장중(open): LIVE(3s TTL) + LAST(영구) 동시 갱신
     * - 마감(close): LAST는 덮어쓰지 않음(동결 유지), 필요 시 LIVE만 선택적으로 갱신 가능
     *
     * @param dto 호가 정보 DTO
     */
    public void save(StockOrderBookDto dto) {
        try {
            String liveKey = getLiveKey(dto.ticker());
            String lastKey = getLastKey(dto.ticker());
            String json = mapper.writeValueAsString(dto);

            if (MarketTimeUtil.isMarketOpen()) {
                redisTemplate.opsForValue().set(liveKey, json, LIVE_TTL);
                redisTemplate.opsForValue().set(lastKey, json);
            } else {
                log.debug("[Redis] 시장 마감 상태 - LAST 스냅샷은 덮어쓰지 않음: {}", dto.ticker());
            }
        } catch (JsonProcessingException e) {
            log.error("[Redis] 호가 저장 실패 - {}", e.getMessage(), e);
        }
    }

    /**
     * 현재 호가 조회 (시장 상태에 따라 소스 선택)
     * - 장중: LIVE 키에서 조회
     * - 마감: LAST 스냅샷에서 조회
     *
     * @param ticker 종목 코드
     * @return 호가 정보 DTO (없으면 null)
     */
    public StockOrderBookDto get(String ticker) {
        boolean open = MarketTimeUtil.isMarketOpen();
        return open ? getLive(ticker) : getLastSnapshot(ticker);
    }

    /**
     * LIVE(3초 TTL) 호가 조회
     */
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

    /**
     * LAST(영구) 마지막 스냅샷 호가 조회
     */
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
