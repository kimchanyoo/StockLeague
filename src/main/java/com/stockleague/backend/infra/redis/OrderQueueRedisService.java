package com.stockleague.backend.infra.redis;

import com.stockleague.backend.stock.domain.Order;
import com.stockleague.backend.stock.domain.OrderType;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderQueueRedisService {

    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "waiting:orders:";

    /**
     * 주문 정보를 Redis ZSET에 저장합니다.
     * <p>ZSET 키는 주문 타입(매수/매도)과 종목 코드로 구성되며,
     * score는 주문 가격으로 사용됩니다.</p>
     *
     * 예: waiting:orders:BUY:005930
     *
     * @param order 저장할 주문 엔티티
     */
    public void saveWaitingOrder(Order order) {
        String key = getKey(order);
        double score = order.getOrderPrice().doubleValue();
        redisTemplate.opsForZSet().add(key, String.valueOf(order.getId()), score);
        log.debug("Redis 주문 대기 큐 등록: {} → {}", key, order.getId());
    }

    /**
     * Redis 키를 반환합니다.
     * <p>형식: waiting:orders:{BUY|SELL}:{ticker}</p>
     * 예: waiting:orders:BUY:005930
     *
     * @param order 주문 엔티티
     * @return Redis ZSET 키 문자열
     */
    private String getKey(Order order) {
        return PREFIX + order.getOrderType().name() + ":" + order.getStock().getStockTicker();
    }

    /**
     * Redis ZSET에서 주어진 주문 타입과 종목 코드에 해당하는
     * 모든 대기 주문 ID를 조회합니다.
     *
     * <p>예시 Redis 키: waiting:orders:BUY:005930</p>
     *
     * @param type 주문 타입 (BUY 또는 SELL)
     * @param ticker 종목 코드
     * @return 주문 ID 리스트 (Long)
     */
    public List<Long> getWaitingOrderIds(OrderType type, String ticker) {
        String key = PREFIX + type.name() + ":" + ticker;
        Set<String> ids = redisTemplate.opsForZSet().range(key, 0, -1);
        if (ids == null) return Collections.emptyList();
        return ids.stream().map(Long::valueOf).collect(Collectors.toList());
    }

    /**
     * Redis ZSET에서 특정 주문 ID를 제거합니다.
     * 체결이 완료된 주문은 대기 큐에서 삭제되어야 합니다.
     *
     * <p>예시 Redis 키: waiting:orders:BUY:005930</p>
     *
     * @param type 주문 타입 (BUY 또는 SELL)
     * @param ticker 종목 코드
     * @param orderId 제거할 주문 ID
     */
    public void removeOrderFromQueue(OrderType type, String ticker, Long orderId) {
        String key = PREFIX + type.name() + ":" + ticker;
        redisTemplate.opsForZSet().remove(key, String.valueOf(orderId));
    }

    /**
     * Redis에 존재하는 모든 대기 주문의 종목 코드 리스트를 반환합니다.
     *
     * <p>Redis 키 패턴: waiting:orders:{type}:{ticker}</p>
     * <p>예: waiting:orders:BUY:005930 → "005930"</p>
     *
     * @return 종목 코드 목록 (중복 제거된 리스트)
     */
    public List<String> getAllTickersWithOrders() {
        Set<String> keys = redisTemplate.keys(PREFIX + "*");
        if (keys == null) return Collections.emptyList();
        return keys.stream()
                .map(key -> key.split(":")[3])
                .distinct()
                .collect(Collectors.toList());
    }

}
