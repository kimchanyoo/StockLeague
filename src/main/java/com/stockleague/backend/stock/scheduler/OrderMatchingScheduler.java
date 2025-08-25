package com.stockleague.backend.stock.scheduler;

import com.stockleague.backend.global.util.MarketTimeUtil;
import com.stockleague.backend.infra.redis.OrderQueueRedisService;
import com.stockleague.backend.infra.redis.StockOrderBookRedisService;
import com.stockleague.backend.stock.domain.OrderType;
import com.stockleague.backend.stock.dto.response.stock.StockOrderBookDto;
import com.stockleague.backend.stock.service.OrderMatchExecutor;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderMatchingScheduler {

    private final OrderQueueRedisService orderQueueRedisService;
    private final StockOrderBookRedisService stockOrderBookRedisService;

    private final OrderMatchExecutor orderMatchExecutor;

    @Scheduled(fixedDelay = 1000)
    public void matchOrdersFromRedis() {
        if(MarketTimeUtil.isMarketClosed()){
            return;
        }
        List<String> tickers = orderQueueRedisService.getAllTickersWithOrders();
        for (String ticker : tickers) {
            processBuyOrders(ticker);
            processSellOrders(ticker);
        }
    }

    private void processBuyOrders(String ticker) {
        StockOrderBookDto orderBook = stockOrderBookRedisService.get(ticker);
        if (orderBook == null) return;

        List<Long> orderIds = orderQueueRedisService.getWaitingOrderIds(OrderType.BUY, ticker);
        for (Long orderId : orderIds) {
            try {
                orderMatchExecutor.processBuyOrder(orderId, ticker, orderBook);
            } catch (Exception e) {
                log.warn("[Match][BUY] orderId={} 처리 실패 (다음 주문 계속)", orderId, e);
            }
        }
    }

    private void processSellOrders(String ticker) {
        StockOrderBookDto orderBook = stockOrderBookRedisService.get(ticker);
        if (orderBook == null) return;

        List<Long> orderIds = orderQueueRedisService.getWaitingOrderIds(OrderType.SELL, ticker);
        for (Long orderId : orderIds) {
            try {
                orderMatchExecutor.processSellOrder(orderId, ticker, orderBook);
            } catch (Exception e) {
                log.warn("[Match][SELL] orderId={} 처리 실패 (다음 주문 계속)", orderId, e);
            }
        }
    }
}