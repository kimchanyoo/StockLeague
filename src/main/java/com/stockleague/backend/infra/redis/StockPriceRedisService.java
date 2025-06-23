package com.stockleague.backend.infra.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockleague.backend.stock.dto.response.stock.StockPriceDto;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockPriceRedisService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PREFIX = "stock:price:";

    /**
     * Redis에 시세를 ZSET으로 저장
     * <p>key: stock:price:{ticker}</p>
     * <p>score: LocalDateTime을 epochSecond로 변환한 값</p>
     * <p>value: JSON 직렬화된 StockPriceDto</p>
     *
     * @param dto 저장할 실시간 시세 데이터
     */
    public void save(StockPriceDto dto) {
        try {
            String key = getKey(dto.ticker());

            Set<String> latestSet = redisTemplate.opsForZSet().reverseRange(key, 0, 0);

            if (latestSet != null && !latestSet.isEmpty()) {
                String latestJson = latestSet.iterator().next();
                StockPriceDto lastDto = objectMapper.readValue(latestJson, StockPriceDto.class);

                if (isDuplicate(lastDto, dto)) {
                    log.debug("Redis 중복 저장 생략: {} (동일 시세)", dto);
                    return;
                }
            }

            String value = objectMapper.writeValueAsString(dto);
            double score = dto.datetime().toInstant(ZoneOffset.ofHours(9)).toEpochMilli();
            redisTemplate.opsForZSet().add(key, value, score);
            log.debug("Redis 저장: [{}] score={}, value={}", key, score, value);

        } catch (JsonProcessingException e) {
            log.error("[Redis] 시세 저장 실패 (직렬화 오류): {}", e.getMessage());
        }
    }

    /**
     * Redis에서 특정 시간 범위의 시세 데이터를 조회
     * <p>key: stock:price:{ticker}</p>
     * <p>score 범위: from ~ to (epochSecond 기준)</p>
     *
     * @param ticker 종목 코드
     * @param from 시작 시각
     * @param to 종료 시각
     * @return 해당 시간 범위의 시세 데이터 목록
     */
    public List<StockPriceDto> findBetween(String ticker, LocalDateTime from, LocalDateTime to) {
        try {
            String key = getKey(ticker);
            double fromScore = from.toEpochSecond(ZoneOffset.ofHours(9));
            double toScore = to.toEpochSecond(ZoneOffset.ofHours(9));

            Set<String> range = redisTemplate.opsForZSet().rangeByScore(key, fromScore, toScore);
            if (range == null) return Collections.emptyList();

            List<StockPriceDto> result = new ArrayList<>();
            for (String json : range) {
                result.add(objectMapper.readValue(json, StockPriceDto.class));
            }
            return result;

        } catch (Exception e) {
            log.error("[Redis] 분봉 데이터 조회 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Redis에서 특정 종목의 시세 데이터를 전부 삭제
     *
     * @param ticker 종목 코드
     */
    public void deleteAll(String ticker) {
        redisTemplate.delete(getKey(ticker));
    }

    /**
     * 시세 Redis 키를 생성
     * <p>형식: stock:price:{ticker}</p>
     *
     * @param ticker 종목 코드
     * @return Redis 키 문자열
     */
    private String getKey(String ticker) {
        return PREFIX + ticker;
    }

    /**
     * Redis에 저장된 이전 시세와 비교하여 중복 여부를 판단
     * <p>datetime과 currentPrice가 동일한 경우 중복으로 간주</p>
     *
     * @param a 이전에 저장된 시세 데이터
     * @param b 새로 수신한 시세 데이터
     * @return 두 시세가 동일하면 true, 그렇지 않으면 false
     */
    private boolean isDuplicate(StockPriceDto a, StockPriceDto b) {
        return a.datetime().equals(b.datetime()) &&
                a.currentPrice() == b.currentPrice();
    }
}

