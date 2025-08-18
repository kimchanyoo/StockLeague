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
            if (shouldIgnore(dto)) {
                log.debug("[Redis] 무효 호가(0 채움) 무시: {}", dto.ticker());
                return;
            }

            String liveKey = getLiveKey(dto.ticker());
            String lastKey = getLastKey(dto.ticker());
            String json = mapper.writeValueAsString(dto);

            if (MarketTimeUtil.isMarketOpen()) {
                redisTemplate.opsForValue().set(liveKey, json, LIVE_TTL);

                String prev = redisTemplate.opsForValue().get(liveKey);
                if (prev == null || shouldIgnore(mapper.readValue(prev, StockOrderBookDto.class))){
                    log.debug("[Redis] LAST 스냅샷 교체: {}", dto.ticker());
                    redisTemplate.opsForValue().set(lastKey, json);
                }else {
                    redisTemplate.opsForValue().set(lastKey, json);
                }
            } else {
                log.debug("[Redis] 시장 마감 상태 - LAST 스냅샷은 덮어쓰지 않음: {}", dto.ticker());
            }
        } catch (JsonProcessingException e) {
            log.error("[Redis] 호가 저장 실패 - {}", e.getMessage(), e);
        }
    }

    /**
     * 주어진 호가 데이터(StockOrderBookDto)가 저장 대상에서 제외해야 할 "무효 데이터"인지 판별합니다.
     *
     * <p>무효 데이터로 간주되는 조건:</p>
     * <ul>
     *   <li>객체가 null인 경우</li>
     *   <li>매도 1호가(askPrices[0])와 매수 1호가(bidPrices[0])가 모두 0인 경우</li>
     *   <li>모든 매도/매수 호가 가격이 0인 경우</li>
     *   <li>모든 매도/매수 호가 수량이 0인 경우</li>
     * </ul>
     *
     * <p>이 메서드는 주로 장 마감 직전 또는 네트워크 오류로 인해 들어오는
     * 0으로 채워진 잘못된 호가 데이터를 Redis에 저장하지 않기 위해 사용됩니다.</p>
     *
     * @param ob 검사할 호가 데이터 객체
     * @return true - 무효 데이터(저장하지 않음), false - 정상 데이터(저장 가능)
     */
    private boolean shouldIgnore(StockOrderBookDto ob) {
        if (ob == null) return true;

        long[] ap = ob.askPrices();
        long[] bp = ob.bidPrices();
        long[] av = ob.askVolumes();
        long[] bv = ob.bidVolumes();

        if (!valid(ap) || !valid(bp) || !valid(av) || !valid(bv)) return true;

        boolean validTopAsk = ap[0] > 0 && av[0] > 0;
        boolean validTopBid = bp[0] > 0 && bv[0] > 0;
        if (!validTopAsk || !validTopBid) return true;

        if (isAllZero(ap) || isAllZero(bp) || isAllZero(av) || isAllZero(bv)) return true;

        if (!isSortedAscPositive(ap) || !isSortedDescPositive(bp)) return true;

        return false;
    }

    /**
     * 배열이 유효한지 검사합니다.
     *
     * <p>유효 조건:</p>
     * <ul>
     *   <li>배열이 null이 아님</li>
     *   <li>배열 길이가 1 이상임</li>
     * </ul>
     *
     * @param arr 검사할 long 배열
     * @return true - 배열이 존재하고 길이가 1 이상일 때, false - null 또는 비어 있음
     */
    private boolean valid(long[] arr) {
        return arr != null && arr.length > 0;
    }

    /**
     * 주어진 long 배열이 모두 0인지 확인합니다.
     *
     * @param arr 검사할 long 배열
     * @return true - 모든 값이 0 또는 배열이 null/비어 있음, false - 0이 아닌 값 존재
     */
    private boolean isAllZero(long[] arr) {
        if (arr == null || arr.length == 0) return true;
        for (long v : arr) if (v != 0) return false;
        return true;
    }

    /**
     * 매도 호가(askPrices)가 오름차순으로 정렬되어 있는지 확인합니다.
     *
     * <p>검사 규칙:</p>
     * <ul>
     *   <li>0보다 큰 값만 검사 대상</li>
     *   <li>앞선 값보다 뒤 값이 작으면 잘못된 정렬로 간주</li>
     *   <li>중간에 0이 나오면 이후 값은 비어있다고 간주하고 검사 종료</li>
     * </ul>
     *
     * @param arr 매도 호가 배열
     * @return true - 정상 오름차순, false - 비정상 정렬
     */
    private boolean isSortedAscPositive(long[] arr) {
        long prev = 0;
        for (long v : arr) {
            if (v == 0) break;
            if (v < prev) return false;
            prev = v;
        }
        return true;
    }

    /**
     * 매수 호가(bidPrices)가 내림차순으로 정렬되어 있는지 확인합니다.
     *
     * <p>검사 규칙:</p>
     * <ul>
     *   <li>0보다 큰 값만 검사 대상</li>
     *   <li>앞선 값보다 뒤 값이 크면 잘못된 정렬로 간주</li>
     *   <li>중간에 0이 나오면 이후 값은 비어있다고 간주하고 검사 종료</li>
     * </ul>
     *
     * @param arr 매수 호가 배열
     * @return true - 정상 내림차순, false - 비정상 정렬
     */
    private boolean isSortedDescPositive(long[] arr) {
        long prev = Long.MAX_VALUE;
        for (long v : arr) {
            if (v == 0) break;
            if (v > prev) return false;
            prev = v;
        }
        return true;
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
