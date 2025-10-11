package com.stockleague.backend.infra.redis;

import com.stockleague.backend.stock.domain.Order;
import com.stockleague.backend.stock.domain.OrderStatus;
import com.stockleague.backend.stock.domain.OrderType;
import com.stockleague.backend.stock.repository.OrderRepository;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRedisCleanupService {

    private static final String SNAPSHOT_PREFIX = "user:asset:closing:";

    private final StringRedisTemplate redisTemplate;
    private final OrderRepository orderRepository;
    private final OrderQueueRedisService orderQueueRedisService;

    /**
     * 유저 삭제 전에 Redis 흔적을 모두 제거한다.
     * - 실시간 체결을 막기 위해 대기/부분체결 주문을 시스템 취소 + 대기 큐에서 제거
     * - 장마감 스냅샷 키(user:asset:closing:*:{userId}) 전부 삭제
     */
    @Transactional
    public void purgeAllForUser(Long userId) {
        cancelAndRemovePendingOrdersFromQueues(userId);
        deleteAllSnapshotKeys(userId);
    }

    /** 대기/부분체결 주문을 시스템 취소하고 Redis 대기 큐에서 제거 */
    private void cancelAndRemovePendingOrdersFromQueues(Long userId) {
        Set<OrderStatus> pending = EnumSet.of(OrderStatus.WAITING, OrderStatus.PARTIALLY_EXECUTED);

        List<com.stockleague.backend.stock.domain.Order> orders = orderRepository.findByUserIdAndStatusIn(userId, pending);

        for (Order o : orders) {
            try {
                o.cancelBySystem();
                orderRepository.save(o);

                OrderType type = o.getOrderType();
                String ticker = o.getStock().getStockTicker();
                orderQueueRedisService.removeOrderFromQueue(type, ticker, o.getId());

                log.info("[RedisCleanup] removed from queue: userId={}, orderId={}, {}:{}",
                        userId, o.getId(), type, ticker);
            } catch (Exception e) {
                log.warn("[RedisCleanup] fail to cancel/remove order from queue: orderId={}, err={}",
                        o.getId(), e.getMessage());
            }
        }
    }

    /** user:asset:closing:*:{userId} 패턴의 모든 스냅샷 키 삭제 (SCAN 사용) */
    private void deleteAllSnapshotKeys(Long userId) {
        String pattern = SNAPSHOT_PREFIX + "*:" + userId;
        int count = scanAndDelete(pattern);
        log.info("[RedisCleanup] deleted {} snapshot keys for userId={}", count, userId);
    }

    /** KEYS 대신 SCAN으로 안전하게 삭제 */
    private int scanAndDelete(String pattern) {
        Integer deleted = redisTemplate.execute((RedisConnection connection) -> {
            int d = 0;
            Cursor<byte[]> cursor = connection.scan(
                    ScanOptions.scanOptions().match(pattern).count(1000).build());

            if (cursor == null) {
                return 0;
            }

            try (cursor) {
                while (cursor.hasNext()) {
                    byte[] key = cursor.next();
                    connection.keyCommands().del(key);
                    d++;
                }
            } catch (Exception e) {
                log.warn("[RedisCleanup] SCAN delete failed: pattern={}, err={}", pattern, e.getMessage());
            }
            return d;
        });

        return deleted == null ? 0 : deleted;
    }
}
