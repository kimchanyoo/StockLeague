package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.infra.redis.OrderQueueRedisService;
import com.stockleague.backend.infra.redis.StockOrderBookRedisService;
import com.stockleague.backend.stock.domain.Order;
import com.stockleague.backend.stock.domain.OrderExecution;
import com.stockleague.backend.stock.domain.OrderType;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.dto.request.order.BuyOrderRequestDto;
import com.stockleague.backend.stock.dto.request.order.SellOrderRequestDto;
import com.stockleague.backend.stock.dto.response.order.BuyOrderResponseDto;
import com.stockleague.backend.stock.dto.response.order.SellOrderResponseDto;
import com.stockleague.backend.stock.dto.response.stock.StockOrderBookDto;
import com.stockleague.backend.stock.repository.OrderExecutionRepository;
import com.stockleague.backend.stock.repository.OrderRepository;
import com.stockleague.backend.stock.repository.StockRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderExecutionRepository orderExecutionRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final OrderQueueRedisService orderQueueRedisService;
    private final StockOrderBookRedisService stockOrderBookRedisService;

    /**
     * 사용자가 종목 티커(ticker)와 주문 정보(가격, 수량)를 기반으로 매수 주문을 생성합니다.
     * <p>
     * 생성된 주문은 DB에 저장되며, 초기 상태는 'WAITING'입니다. 체결 여부는 이후 체결 처리 로직을 통해 업데이트됩니다.
     *
     * @param userId     현재 로그인한 사용자 ID
     * @param requestDto 매수 요청 정보 (ticker, price, amount) {@link BuyOrderRequestDto}
     * @return 매수 주문 접수 결과 응답 DTO
     */
    @Transactional
    public BuyOrderResponseDto buy(Long userId, BuyOrderRequestDto requestDto) {
        User user = getUserById(userId);
        Stock stock = getStockByTicker(requestDto.ticker());

        Order order = Order.builder()
                .user(user)
                .stock(stock)
                .orderType(OrderType.BUY)
                .orderPrice(requestDto.orderPrice())
                .orderAmount(requestDto.orderAmount())
                .remainingAmount(requestDto.orderAmount())
                .averageExecutedPrice(requestDto.orderPrice())
                .build();

        orderRepository.save(order);

        BigDecimal remaining = tryImmediateBuyExecution(order);
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            orderQueueRedisService.saveWaitingOrder(order);
        }

        return BuyOrderResponseDto.from();
    }

    /**
     * 사용자가 종목 티커(ticker)와 주문 정보(가격, 수량)를 기반으로 매도 주문을 생성합니다.
     * <p>
     * 생성된 주문은 DB에 저장되며, 초기 상태는 'WAITING'입니다. 체결 여부는 이후 체결 처리 로직을 통해 업데이트됩니다.
     *
     * @param userId     현재 로그인한 사용자 ID
     * @param requestDto 매수 요청 정보 (ticker, price, amount) {@link BuyOrderRequestDto}
     * @return 매수 주문 접수 결과 응답 DTO
     */
    @Transactional
    public SellOrderResponseDto sell(Long userId, SellOrderRequestDto requestDto) {
        User user = getUserById(userId);
        Stock stock = getStockByTicker(requestDto.ticker());

        Order order = Order.builder()
                .user(user)
                .stock(stock)
                .orderType(OrderType.SELL)
                .orderPrice(requestDto.orderPrice())
                .orderAmount(requestDto.orderAmount())
                .remainingAmount(requestDto.orderAmount())
                .averageExecutedPrice(requestDto.orderPrice())
                .build();

        orderRepository.save(order);

        BigDecimal remaining = tryImmediateSellExecution(order);
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            orderQueueRedisService.saveWaitingOrder(order);
        }

        return SellOrderResponseDto.from();
    }

    /**
     * 사용자 ID를 기반으로 사용자 정보를 조회합니다. 존재하지 않을 경우 USER_NOT_FOUND 예외를 발생시킵니다.
     *
     * @param userId 조회할 사용자 ID
     * @return User 엔티티
     * @throws GlobalException USER_NOT_FOUND
     */
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));
    }

    /**
     * 종목 티커를 기반으로 종목 정보를 조회합니다. 존재하지 않을 경우 STOCK_NOT_FOUND 예외를 발생시킵니다.
     *
     * @param ticker 조회할 종목 티커
     * @return Stock 엔티티
     * @throws GlobalException STOCK_NOT_FOUND
     */
    private Stock getStockByTicker(String ticker) {
        return stockRepository.findByStockTicker(ticker)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.STOCK_NOT_FOUND));
    }

    /**
     * 현재 Redis에 저장된 호가 정보를 기반으로 주어진 매수 주문을 즉시 체결 가능한지 확인하고, 가능한 범위 내에서 부분 혹은 전체 체결을 수행합니다.
     * <ul>
     *     <li>호가 정보는 Redis에서 가져옵니다.</li>
     *     <li>주문 가격 이상인 매도 호가(ask)와 체결 가능 수량을 비교하여 매칭합니다.</li>
     *     <li>실제 체결된 내역은 OrderExecution 리스트에 저장되고 DB에 반영됩니다.</li>
     *     <li>주문 객체는 체결 수량, 평균 체결가, 상태 등을 갱신합니다.</li>
     * </ul>
     *
     * @param order 매수 주문 객체
     * @return 체결 후 남은 주문 수량 (remainingAmount)
     */
    private BigDecimal tryImmediateBuyExecution(Order order) {
        StockOrderBookDto orderBook = stockOrderBookRedisService.get(order.getStock().getStockTicker());
        if (orderBook == null) {
            return order.getRemainingAmount();
        }

        BigDecimal remaining = order.getRemainingAmount();
        BigDecimal executedTotal = BigDecimal.ZERO;
        BigDecimal totalPrice = BigDecimal.ZERO;

        long[] askPrices = orderBook.askPrices();
        long[] askVolumes = orderBook.askVolumes();

        List<OrderExecution> executions = new ArrayList<>();

        for (int i = 0; i < askPrices.length; i++) {
            long price = askPrices[i];
            long volume = askVolumes[i];

            if (price <= 0 || volume <= 0) {
                continue;
            }

            BigDecimal askPrice = BigDecimal.valueOf(price);
            BigDecimal askVolume = BigDecimal.valueOf(volume);

            if (order.getOrderPrice().compareTo(askPrice) >= 0) {
                BigDecimal matchedAmount = remaining.min(askVolume);

                executions.add(OrderExecution.builder()
                        .order(order)
                        .executedPrice(askPrice)
                        .executedAmount(matchedAmount)
                        .build());

                executedTotal = executedTotal.add(matchedAmount);
                totalPrice = totalPrice.add(matchedAmount.multiply(askPrice));
                remaining = remaining.subtract(matchedAmount);
            }

            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
        }

        if (!executions.isEmpty()) {
            orderExecutionRepository.saveAll(executions);
            order.updateExecutionInfo(executedTotal, totalPrice);
        }

        return remaining;
    }

    /**
     * 현재 Redis에 저장된 호가 정보를 기반으로 주어진 매도 주문을 즉시 체결 가능한지 확인하고,
     * 가능한 범위 내에서 부분 혹은 전체 체결을 수행합니다.
     * <ul>
     *     <li>호가 정보는 Redis에서 가져옵니다.</li>
     *     <li>주문 가격 이하인 매수 호가(bid)와 체결 가능 수량을 비교하여 매칭합니다.</li>
     *     <li>실제 체결된 내역은 OrderExecution 리스트에 저장되고 DB에 반영됩니다.</li>
     *     <li>주문 객체는 체결 수량, 평균 체결가, 상태 등을 갱신합니다.</li>
     * </ul>
     *
     * @param order 매도 주문 객체
     * @return 체결 후 남은 주문 수량 (remainingAmount)
     */
    private BigDecimal tryImmediateSellExecution(Order order) {
        StockOrderBookDto orderBook = stockOrderBookRedisService.get(order.getStock().getStockTicker());
        if (orderBook == null) {
            return order.getRemainingAmount();
        }

        BigDecimal remaining = order.getRemainingAmount();
        BigDecimal executedTotal = BigDecimal.ZERO;
        BigDecimal totalPrice = BigDecimal.ZERO;

        long[] bidPrices = orderBook.bidPrices();
        long[] bidVolumes = orderBook.bidVolumes();

        List<OrderExecution> executions = new ArrayList<>();

        for (int i = 0; i < bidPrices.length; i++) {
            long price = bidPrices[i];
            long volume = bidVolumes[i];

            if (price <= 0 || volume <= 0) {
                continue;
            }

            BigDecimal bidPrice = BigDecimal.valueOf(price);
            BigDecimal bidVolume = BigDecimal.valueOf(volume);

            if (bidPrice.compareTo(order.getOrderPrice()) >= 0) {
                BigDecimal matchedAmount = remaining.min(bidVolume);

                executions.add(OrderExecution.builder()
                        .order(order)
                        .executedPrice(bidPrice)
                        .executedAmount(matchedAmount)
                        .build());

                executedTotal = executedTotal.add(matchedAmount);
                totalPrice = totalPrice.add(matchedAmount.multiply(bidPrice));
                remaining = remaining.subtract(matchedAmount);
            }

            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
        }

        if (!executions.isEmpty()) {
            orderExecutionRepository.saveAll(executions);
            order.updateExecutionInfo(executedTotal, totalPrice);
        }

        return remaining;
    }

}
