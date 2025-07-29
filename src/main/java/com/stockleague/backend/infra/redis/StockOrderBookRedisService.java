package com.stockleague.backend.infra.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final String PREFIX = "stock:orderbook:";

    /**
     * 호가 정보를 Redis에 저장
     *
     * @param dto 호가 정보 DTO
     */
    public void save(StockOrderBookDto dto) {
        try {
            String key = getKey(dto.ticker());
            String value = mapper.writeValueAsString(dto);
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(3));
            log.info("[Redis] 호가 저장 완료");
        } catch (JsonProcessingException e) {
            log.error("[Redis] 호가 저장 실패 - {}", e.getMessage(), e);
        }
    }

    /**
     * Redis에서 현재 호가 정보를 조회
     *
     * @param ticker 종목 코드
     * @return 호가 정보 DTO (없으면 null)
     */
    public StockOrderBookDto get(String ticker) {
        try {
            String key = getKey(ticker);
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) return null;
            return mapper.readValue(json, StockOrderBookDto.class);
        } catch (Exception e) {
            log.error("[Redis] 호가 조회 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    private String getKey(String ticker) {
        return PREFIX + ticker;
    }
}
