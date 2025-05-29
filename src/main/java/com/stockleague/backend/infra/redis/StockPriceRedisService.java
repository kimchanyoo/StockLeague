package com.stockleague.backend.infra.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockleague.backend.stock.dto.response.stock.StockPriceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockPriceRedisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PREFIX = "stock:";
    private static final Duration TTL = Duration.ofSeconds(3);

    public void save(StockPriceDto dto) {
        try {
            String key = getKey(dto.ticker());
            String value = objectMapper.writeValueAsString(dto);
            redisTemplate.opsForValue().set(key, value, TTL);
        } catch (JsonProcessingException e) {
            log.error("[Redis] 시세 저장 실패: {}", e.getMessage());
        }
    }

    public Optional<StockPriceDto> findByTicker(String ticker) {
        try {
            String key = getKey(ticker);
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return Optional.of(objectMapper.readValue(value, StockPriceDto.class));
            }
        } catch (Exception e) {
            log.error("[Redis] 시세 조회 실패: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private String getKey(String ticker) {
        return PREFIX + ticker + ":price";
    }
}

