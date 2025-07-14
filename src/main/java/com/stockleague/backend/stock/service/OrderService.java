package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.infra.redis.OrderQueueRedisService;
import com.stockleague.backend.infra.redis.StockOrderBookRedisService;
import com.stockleague.backend.stock.domain.Order;
import com.stockleague.backend.stock.domain.OrderExecution;
import com.stockleague.backend.stock.domain.OrderType;
import com.stockleague.backend.stock.domain.ReservedCash;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.dto.request.order.BuyOrderRequestDto;
import com.stockleague.backend.stock.dto.request.order.SellOrderRequestDto;
import com.stockleague.backend.stock.dto.response.order.BuyOrderResponseDto;
import com.stockleague.backend.stock.dto.response.order.CancelOrderResponseDto;
import com.stockleague.backend.stock.dto.response.order.SellOrderResponseDto;
import com.stockleague.backend.stock.dto.response.stock.StockOrderBookDto;
import com.stockleague.backend.stock.repository.OrderExecutionRepository;
import com.stockleague.backend.stock.repository.OrderRepository;
import com.stockleague.backend.stock.repository.ReservedCashRepository;
import com.stockleague.backend.stock.repository.StockRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.domain.UserAsset;
import com.stockleague.backend.user.domain.UserStock;
import com.stockleague.backend.user.repository.UserRepository;
import com.stockleague.backend.user.repository.UserStockRepository;
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
    private final ReservedCashRepository reservedCashRepository;
    private final UserStockRepository userStockRepository;
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

        BigDecimal reservedAmount = order.getOrderPrice().multiply(order.getOrderAmount());
        reserveCash(user, order, reservedAmount);

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

        lockSellStock(user, stock, requestDto.orderAmount());

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
     * @throws GlobalException USER_NOT_FOUND - 유저가 존재하지 않는 경우
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
     * @throws GlobalException STOCK_NOT_FOUND - 주식이 존재하지 않는 경우
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

            applyBuyStock(order.getUser(), order.getStock(), executedTotal);

            if (order.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
                finalizeReservedCash(order, totalPrice);
            }
        }

        return remaining;
    }

    /**
     * 현재 Redis에 저장된 호가 정보를 기반으로 주어진 매도 주문을 즉시 체결 가능한지 확인하고, 가능한 범위 내에서 부분 혹은 전체 체결을 수행합니다.
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
            finalizeSellStock(order.getUser(), order.getStock(), executedTotal);
            applySellRevenue(order.getUser(), totalPrice);
        }

        return remaining;
    }

    /**
     * 매수 체결 시, 유저의 보유 주식 정보를 갱신합니다.
     * <ul>
     *     <li>UserStock이 없으면 새로 생성합니다.</li>
     *     <li>기존에 보유하고 있으면 수량을 증가시킵니다.</li>
     * </ul>
     *
     * @param user   사용자
     * @param stock  종목
     * @param amount 매수 체결 수량
     */
    private void applyBuyStock(User user, Stock stock, BigDecimal amount) {
        UserStock userStock = userStockRepository.findByUserAndStock(user, stock)
                .orElse(null);

        if (userStock == null) {
            userStock = UserStock.builder()
                    .user(user)
                    .stock(stock)
                    .quantity(amount)
                    .lockedQuantity(BigDecimal.ZERO)
                    .build();
        } else {
            userStock.increaseQuantity(amount);
        }

        userStockRepository.save(userStock);
    }

    /**
     * 매도 주문 시, 유저가 보유한 주식 수량을 동결합니다.
     * <ul>
     *     <li>보유 수량이 부족한 경우 예외를 발생시킵니다.</li>
     *     <li>동결 수량은 주문 수량만큼 증가합니다.</li>
     * </ul>
     *
     * @param user   매도하는 사용자
     * @param stock  매도할 종목
     * @param amount 매도 수량
     * @throws GlobalException 다음과 같은 예외가 발생할 수 있습니다:
     *          <ul>
     *              <li>{@code USER_STOCK_NOT_FOUND} - 사용자가 해당 종목을 보유하고 있지 않은 경우</li>
     *              <li>{@code NOT_ENOUGH_STOCK} - 사용자가 해당 종목을 충분하게 보유하고 있지 않은 경우</li>
     *          </ul>
     */
    private void lockSellStock(User user, Stock stock, BigDecimal amount) {
        UserStock userStock = userStockRepository.findByUserAndStock(user, stock)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_STOCK_NOT_FOUND));

        if (!userStock.hasEnoughForSell(amount)) {
            throw new GlobalException(GlobalErrorCode.NOT_ENOUGH_STOCK);
        }

        userStock.lockQuantity(amount);
    }

    /**
     * 매도 주문 체결 시, 동결된 주식 수량을 실제 매도된 만큼 차감합니다.
     * <ul>
     *     <li>매도 체결 수량만큼 lockedQuantity 감소</li>
     *     <li>보유 수량(quantity)도 함께 감소</li>
     * </ul>
     *
     * @param user           사용자
     * @param stock          종목
     * @param executedAmount 체결 수량
     * @throws GlobalException USER_STOCK_NOT_FOUND - 사용자가 해당 종목을 보유하고 있지 않은 경우
     */
    private void finalizeSellStock(User user, Stock stock, BigDecimal executedAmount) {
        UserStock userStock = userStockRepository.findByUserAndStock(user, stock)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_STOCK_NOT_FOUND));

        userStock.executeSell(executedAmount);
    }



    /**
     * 매도 체결 금액을 유저 자산에 추가합니다.
     * <ul>
     *     <li>유저의 현금 자산(cashBalance)을 체결 금액만큼 증가시킵니다.</li>
     * </ul>
     *
     * @param user    사용자
     * @param revenue 매도 수익
     * @throws GlobalException ASSET_NOT_FOUND - 사용자의 자산 정보가 존재하지 않은 경우
     */
    private void applySellRevenue(User user, BigDecimal revenue) {
        UserAsset asset = user.getUserAsset();
        if (asset == null) {
            throw new GlobalException(GlobalErrorCode.USER_ASSET_NOT_FOUND);
        }
        asset.addCash(revenue);
    }

    /**
     * 매수 주문 시, 사용자의 현금 자산에서 주문 금액만큼을 예약 처리합니다.
     * <ul>
     *     <li>유저의 보유 현금이 주문 금액보다 적을 경우 예외를 발생시킵니다.</li>
     *     <li>자산에서 주문 금액을 차감하고, 해당 주문에 대한 예약 현금 정보를 생성하여 저장합니다.</li>
     *     <li>Order와 ReservedCash는 1:1 관계로 연관 설정됩니다.</li>
     * </ul>
     *
     * @param user   주문을 생성한 사용자
     * @param order  현금을 예약할 주문 객체
     * @param amount 예약할 금액 (주문 가격 × 주문 수량)
     * @throws GlobalException NOT_ENOUGH_CASH - 사용자의 보유 현금이 부족한 경우
     */
    private void reserveCash(User user, Order order, BigDecimal amount) {
        UserAsset asset = user.getUserAsset();
        if (asset == null || asset.getCashBalance().compareTo(amount) < 0) {
            throw new GlobalException(GlobalErrorCode.NOT_ENOUGH_CASH);
        }

        asset.subtractCash(amount);

        ReservedCash reservedCash = ReservedCash.builder()
                .user(user)
                .order(order)
                .reservedAmount(amount)
                .refunded(false)
                .build();

        reservedCashRepository.save(reservedCash);
        order.setReservedCash(reservedCash);
    }

    /**
     * 매수 주문 체결 완료 후 예약된 현금을 정산하고 남은 금액을 환불합니다.
     * <ul>
     *     <li>실제 체결 금액이 예약 금액보다 적은 경우, 차액을 유저 자산에 환불합니다.</li>
     *     <li>환불 여부는 ReservedCash 엔티티의 refunded 필드를 true로 설정합니다.</li>
     * </ul>
     *
     * @param order        체결된 주문
     * @param actualCost   실제 체결 금액
     * @throws GlobalException RESERVED_CASH_NOT_FOUND - 예약 현금 정보가 없는 경우
     */
    private void finalizeReservedCash(Order order, BigDecimal actualCost) {
        ReservedCash reservedCash = reservedCashRepository.findByOrder(order)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.RESERVED_CASH_NOT_FOUND));

        BigDecimal reserved = reservedCash.getReservedAmount();
        BigDecimal refund = reserved.subtract(actualCost);

        if (refund.compareTo(BigDecimal.ZERO) > 0) {
            UserAsset asset = order.getUser().getUserAsset();
            if (asset == null) {
                throw new GlobalException(GlobalErrorCode.USER_ASSET_NOT_FOUND);
            }
            asset.addCash(refund);
        }

        reservedCash.markAsRefunded();
    }

    /**
     * 매도 주문이 체결되지 않은 경우, 동결된 주식 수량을 해제합니다.
     * <ul>
     *     <li>매도 주문 취소 또는 미체결 상태에서 유저의 lockedQuantity를 복원합니다.</li>
     *     <li>보유 수량(quantity)에는 영향을 주지 않습니다.</li>
     * </ul>
     *
     * @param user   사용자
     * @param stock  종목
     * @param amount 해제할 수량
     * @throws GlobalException USER_STOCK_NOT_FOUND - 사용자가 해당 종목을 보유하고 있지 않은 경우
     */
    private void unlockSellStock(User user, Stock stock, BigDecimal amount) {
        UserStock userStock = userStockRepository.findByUserAndStock(user, stock)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_STOCK_NOT_FOUND));

        userStock.unlockQuantity(amount);
    }

    /**
     * 사용자의 매수 또는 매도 주문을 취소합니다.
     * <p>
     * 취소 요청은 다음 조건을 만족해야 합니다:
     * <ul>
     *     <li>주문이 완료(EXECUTED)되거나 취소(CANCELED)되지 않은 상태여야 합니다.</li>
     *     <li>해당 주문의 소유자(userId)만 취소할 수 있습니다.</li>
     * </ul>
     * 주문 유형에 따른 처리 방식은 다음과 같습니다:
     * <ul>
     *     <li><b>매수 주문 (BUY)</b>: 예약된 현금을 환불하고, 예약 상태를 'refunded'로 표시합니다.</li>
     *     <li><b>매도 주문 (SELL)</b>: 동결된 주식 수량(lockedQuantity)을 해제합니다.</li>
     * </ul>
     * 또한 주문은 Redis 대기 큐에서 제거되며, 남은 주문 수량은 0으로 설정됩니다.
     *
     * @param userId  주문 취소를 요청한 사용자 ID
     * @param orderId 취소할 주문 ID
     * @throws GlobalException 다음과 같은 예외가 발생할 수 있습니다:
     *          <ul>
     *              <li>{@code ORDER_NOT_FOUND} - 주문이 존재하지 않는 경우</li>
     *              <li>{@code UNAUTHORIZED_ORDER_ACCESS} - 다른 사용자의 주문을 취소하려는 경우</li>
     *              <li>{@code INVALID_ORDER_STATE} - 이미 완료되었거나 취소된 주문인 경우</li>
     *              <li>{@code RESERVED_CASH_NOT_FOUND} - 매수 주문의 예약 현금 정보가 없는 경우</li>
     *              <li>{@code USER_ASSET_NOT_FOUND} - 사용자 자산 정보가 존재하지 않는 경우</li>
     *          </ul>
     */
    @Transactional
    public CancelOrderResponseDto cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userId)) {
            throw new GlobalException(GlobalErrorCode.UNAUTHORIZED_ORDER_ACCESS);
        }

        if (order.isCompletedOrCanceled()) {
            throw new GlobalException(GlobalErrorCode.INVALID_ORDER_STATE);
        }

        order.markAsCanceled();
        order.setRemainingAmount(BigDecimal.ZERO);

        orderQueueRedisService.removeOrderFromQueue(
                order.getOrderType(),
                order.getStock().getStockTicker(),
                order.getId()
        );

        if (order.getOrderType() == OrderType.BUY) {
            ReservedCash reservedCash = reservedCashRepository.findByOrder(order)
                    .orElseThrow(() -> new GlobalException(GlobalErrorCode.RESERVED_CASH_NOT_FOUND));

            if (!reservedCash.isRefunded()) {
                BigDecimal refundAmount = reservedCash.getReservedAmount();
                UserAsset asset = order.getUser().getUserAsset();
                if (asset == null) {
                    throw new GlobalException(GlobalErrorCode.USER_ASSET_NOT_FOUND);
                }
                asset.addCash(refundAmount);
                reservedCash.markAsRefunded();
            }

        } else {
            unlockSellStock(order.getUser(), order.getStock(), order.getRemainingAmount());
        }

        return CancelOrderResponseDto.from();
    }
}
