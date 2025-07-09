package com.stockleague.backend.stock.scheduler;

import com.stockleague.backend.infra.redis.OrderQueueRedisService;
import com.stockleague.backend.infra.redis.StockOrderBookRedisService;
import com.stockleague.backend.stock.domain.Order;
import com.stockleague.backend.stock.domain.OrderExecution;
import com.stockleague.backend.stock.domain.OrderType;
import com.stockleague.backend.stock.dto.response.stock.StockOrderBookDto;
import com.stockleague.backend.stock.repository.OrderExecutionRepository;
import com.stockleague.backend.stock.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMatchingScheduler {

    private final OrderQueueRedisService orderQueueRedisService;
    private final StockOrderBookRedisService stockOrderBookRedisService;
    private final OrderRepository orderRepository;
    private final OrderExecutionRepository orderExecutionRepository;

    /**
     * 1초마다 Redis에 있는 대기 주문들을 확인하여 체결 가능한 주문을 처리
     */
    @Scheduled(fixedDelay = 1000)
    public void matchOrdersFromRedis() {
        List<String> tickers = orderQueueRedisService.getAllTickersWithOrders(); // 커스텀 구현 필요
        for (String ticker : tickers) {
            processBuyOrders(ticker);
            processSellOrders(ticker);
        }
    }

    /**
     * Redis에 저장된 매수 대기 주문 목록을 조회하여,
     * 현재 호가 정보를 기준으로 즉시 체결 가능한 주문을 처리합니다.
     *
     * <p>주요 처리 흐름:</p>
     * <ol>
     *     <li>Redis에 저장된 해당 종목의 매수 대기 주문 ID 리스트를 조회</li>
     *     <li>각 주문을 DB에서 조회하고, 호가 정보의 매도 가격과 비교</li>
     *     <li>주문 가격 이상으로 매도 호가가 존재할 경우 부분/전체 체결 시도</li>
     *     <li>체결된 내역을 OrderExecution으로 저장</li>
     *     <li>Order 엔티티의 체결 수량 및 평균 체결가 갱신</li>
     *     <li>잔여 수량이 없으면 Redis 대기 큐에서 제거</li>
     * </ol>
     *
     * @param ticker 종목 코드 (예: "005930")
     */
    private void processBuyOrders(String ticker) {
        StockOrderBookDto orderBook = stockOrderBookRedisService.get(ticker);
        if (orderBook == null) return;

        List<Long> orderIds = orderQueueRedisService.getWaitingOrderIds(OrderType.BUY, ticker);

        for (Long orderId : orderIds) {
            Optional<Order> optionalOrder = orderRepository.findById(orderId);
            if (optionalOrder.isEmpty()) continue;

            Order order = optionalOrder.get();
            BigDecimal remaining = order.getRemainingAmount();

            List<OrderExecution> executions = new ArrayList<>();
            BigDecimal executedAmount = BigDecimal.ZERO;
            BigDecimal totalExecutedPrice = BigDecimal.ZERO;

            long[] askPrices = orderBook.askPrices();
            long[] askVolumes = orderBook.askVolumes();

            for (int i = 0; i < askPrices.length; i++) {
                BigDecimal price = BigDecimal.valueOf(askPrices[i]);
                BigDecimal volume = BigDecimal.valueOf(askVolumes[i]);

                if (price.compareTo(order.getOrderPrice()) > 0) break;
                if (volume.compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal matchedAmount = remaining.min(volume);
                executions.add(OrderExecution.builder()
                        .order(order)
                        .executedAmount(matchedAmount)
                        .executedPrice(price)
                        .build());

                executedAmount = executedAmount.add(matchedAmount);
                totalExecutedPrice = totalExecutedPrice.add(price.multiply(matchedAmount));
                remaining = remaining.subtract(matchedAmount);

                if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            }

            if (!executions.isEmpty()) {
                orderExecutionRepository.saveAll(executions);
                order.updateExecutionInfo(executedAmount, totalExecutedPrice);
                orderRepository.save(order);

                log.info("매수 주문 체결 완료: orderId={}, 체결량={}, 잔여량={}", order.getId(), executedAmount, order.getRemainingAmount());

                if (order.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    orderQueueRedisService.removeOrderFromQueue(OrderType.BUY, ticker, order.getId());
                }
            }
        }
    }

    /**
     * Redis에 저장된 매도 대기 주문 목록을 조회하여,
     * 현재 호가 정보를 기준으로 즉시 체결 가능한 주문을 처리합니다.
     *
     * <p>주요 처리 흐름:</p>
     * <ol>
     *     <li>Redis에 저장된 해당 종목의 매도 대기 주문 ID 리스트를 조회</li>
     *     <li>각 주문을 DB에서 조회하고, 호가 정보의 매수 가격과 비교</li>
     *     <li>주문 가격 이하로 매수 호가가 존재할 경우 부분/전체 체결 시도</li>
     *     <li>체결된 내역을 OrderExecution으로 저장</li>
     *     <li>Order 엔티티의 체결 수량 및 평균 체결가 갱신</li>
     *     <li>잔여 수량이 없으면 Redis 대기 큐에서 제거</li>
     * </ol>
     *
     * @param ticker 종목 코드 (예: "005930")
     */
    private void processSellOrders(String ticker) {
        StockOrderBookDto orderBook = stockOrderBookRedisService.get(ticker);
        if (orderBook == null) return;

        List<Long> orderIds = orderQueueRedisService.getWaitingOrderIds(OrderType.SELL, ticker);

        for (Long orderId : orderIds) {
            Optional<Order> optionalOrder = orderRepository.findById(orderId);
            if (optionalOrder.isEmpty()) continue;

            Order order = optionalOrder.get();
            BigDecimal remaining = order.getRemainingAmount();

            List<OrderExecution> executions = new ArrayList<>();
            BigDecimal executedAmount = BigDecimal.ZERO;
            BigDecimal totalExecutedPrice = BigDecimal.ZERO;

            long[] bidPrices = orderBook.bidPrices();
            long[] bidVolumes = orderBook.bidVolumes();

            for (int i = 0; i < bidPrices.length; i++) {
                BigDecimal price = BigDecimal.valueOf(bidPrices[i]);
                BigDecimal volume = BigDecimal.valueOf(bidVolumes[i]);

                if (price.compareTo(order.getOrderPrice()) < 0) break;
                if (volume.compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal matchedAmount = remaining.min(volume);
                executions.add(OrderExecution.builder()
                        .order(order)
                        .executedAmount(matchedAmount)
                        .executedPrice(price)
                        .build());

                executedAmount = executedAmount.add(matchedAmount);
                totalExecutedPrice = totalExecutedPrice.add(price.multiply(matchedAmount));
                remaining = remaining.subtract(matchedAmount);

                if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            }

            if (!executions.isEmpty()) {
                orderExecutionRepository.saveAll(executions);
                order.updateExecutionInfo(executedAmount, totalExecutedPrice);
                orderRepository.save(order);

                log.info("매도 주문 체결 완료: orderId={}, 체결량={}, 잔여량={}", order.getId(), executedAmount, order.getRemainingAmount());

                if (order.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    orderQueueRedisService.removeOrderFromQueue(OrderType.SELL, ticker, order.getId());
                }
            }
        }
    }
}