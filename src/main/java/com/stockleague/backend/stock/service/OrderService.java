package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.global.util.MarketTimeUtil;
import com.stockleague.backend.infra.redis.OrderQueueRedisService;
import com.stockleague.backend.infra.redis.StockOrderBookRedisService;
import com.stockleague.backend.stock.domain.Order;
import com.stockleague.backend.stock.domain.OrderSession;
import com.stockleague.backend.stock.domain.OrderType;
import com.stockleague.backend.stock.domain.ReservedCash;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.dto.request.order.BuyOrderRequestDto;
import com.stockleague.backend.stock.dto.request.order.SellOrderRequestDto;
import com.stockleague.backend.stock.dto.response.order.BuyOrderResponseDto;
import com.stockleague.backend.stock.dto.response.order.CancelOrderResponseDto;
import com.stockleague.backend.stock.dto.response.order.OrderListResponseDto;
import com.stockleague.backend.stock.dto.response.order.OrderSummaryDto;
import com.stockleague.backend.stock.dto.response.order.SellOrderResponseDto;
import com.stockleague.backend.stock.dto.response.stock.StockOrderBookDto;
import com.stockleague.backend.stock.repository.OrderRepository;
import com.stockleague.backend.stock.repository.ReservedCashRepository;
import com.stockleague.backend.stock.repository.StockRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.domain.UserAsset;
import com.stockleague.backend.user.domain.UserStock;
import com.stockleague.backend.user.repository.UserRepository;
import com.stockleague.backend.user.repository.UserStockRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;
    private final ReservedCashRepository reservedCashRepository;
    private final UserStockRepository userStockRepository;
    private final OrderQueueRedisService orderQueueRedisService;

    private final OrderMatchExecutor orderMatchExecutor;
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

        boolean regular = MarketTimeUtil.isMarketOpen();

        Order order = Order.builder()
                .user(user)
                .stock(stock)
                .orderType(OrderType.BUY)
                .orderPrice(requestDto.orderPrice())
                .orderAmount(requestDto.orderAmount())
                .remainingAmount(requestDto.orderAmount())
                .averageExecutedPrice(requestDto.orderPrice())
                .session(regular ? OrderSession.REGULAR : OrderSession.AFTER_HOURS_QUEUED)
                .build();

        orderRepository.save(order);

        BigDecimal reservedAmount = order.getOrderPrice().multiply(order.getOrderAmount());
        reserveCash(user, order, reservedAmount);

        BigDecimal remaining;
        if (regular) {
            StockOrderBookDto ob = stockOrderBookRedisService.get(stock.getStockTicker());
            if (ob == null) {
                remaining = order.getRemainingAmount();
            } else {
                remaining = orderMatchExecutor.processBuyOrder(order.getId(), stock.getStockTicker(), ob);
            }
        } else {
            remaining = order.getRemainingAmount();
        }

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

        boolean regular = MarketTimeUtil.isMarketOpen();

        lockSellStock(user, stock, requestDto.orderAmount());

        Order order = Order.builder()
                .user(user)
                .stock(stock)
                .orderType(OrderType.SELL)
                .orderPrice(requestDto.orderPrice())
                .orderAmount(requestDto.orderAmount())
                .remainingAmount(requestDto.orderAmount())
                .averageExecutedPrice(requestDto.orderPrice())
                .session(regular ? OrderSession.REGULAR : OrderSession.AFTER_HOURS_QUEUED)
                .build();

        orderRepository.save(order);

        BigDecimal remaining;
        if (regular) {
            StockOrderBookDto ob = stockOrderBookRedisService.get(stock.getStockTicker());
            if (ob == null) {
                remaining = order.getRemainingAmount();
            } else {
                remaining = orderMatchExecutor.processSellOrder(order.getId(), stock.getStockTicker(), ob);
            }
        } else {
            remaining = order.getRemainingAmount();
        }

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
                .build();

        reservedCashRepository.save(reservedCash);
        order.setReservedCash(reservedCash);
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
        UserStock us = userStockRepository.findByUserAndStock(user, stock)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_STOCK_NOT_FOUND));

        BigDecimal beforeLocked = us.getLockedQuantity();
        BigDecimal beforeQty    = us.getQuantity();

        us.unlockQuantity(amount);
        userStockRepository.save(us);

        log.info("[UserStock][CANCEL] userId={}, ticker={}, locked {} -> {}, qty {} -> {} (unlock={})",
                user.getId(),
                stock.getStockTicker(),
                beforeLocked.stripTrailingZeros().toPlainString(),
                us.getLockedQuantity().stripTrailingZeros().toPlainString(),
                beforeQty.stripTrailingZeros().toPlainString(),
                us.getQuantity().stripTrailingZeros().toPlainString(),
                amount.stripTrailingZeros().toPlainString());
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

        BigDecimal remainingBeforeCancel = order.getRemainingAmount();

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
                BigDecimal executedQty = order.getOrderAmount().subtract(remainingBeforeCancel);
                BigDecimal actualCost = executedQty.multiply(order.getAverageExecutedPrice());

                BigDecimal reserved = reservedCash.getReservedAmount();
                BigDecimal refundAmount = reserved.subtract(actualCost);
                if (refundAmount.compareTo(BigDecimal.ZERO) < 0) {
                    refundAmount = BigDecimal.ZERO;
                }

                UserAsset asset = order.getUser().getUserAsset();
                if (asset == null) {
                    throw new GlobalException(GlobalErrorCode.USER_ASSET_NOT_FOUND);
                }
                asset.addCash(refundAmount);
                reservedCash.markAsRefunded(refundAmount);
            }

        } else {
            unlockSellStock(order.getUser(), order.getStock(), remainingBeforeCancel);
        }

        return CancelOrderResponseDto.from();
    }

    /**
     * 사용자의 주문 내역을 페이지 단위로 조회합니다.
     * <p>
     * 주문은 작성 시각(createdAt) 기준 내림차순으로 정렬되며,
     * 매수(BUY), 매도(SELL) 구분 없이 전체 주문 내역이 포함됩니다.
     * </p>
     *
     * @param userId 조회 대상 사용자 ID
     * @param page   조회할 페이지 번호 (1부터 시작)
     * @param size   페이지당 항목 수
     * @return 사용자의 주문 내역 리스트와 페이지 정보가 포함된 응답 DTO {@link OrderListResponseDto}
     * @throws GlobalException 다음과 같은 예외가 발생할 수 있습니다:
     *          <ul>
     *              <li>{@code INVALID_PAGINATION} - 페이지 번호 또는 크기가 1 미만인 경우</li>
     *              <li>{@code USER_NOT_FOUND} - 사용자가 존재하지 않는 경우</li>
     *          </ul>
     */
    @Transactional(readOnly = true)
    public OrderListResponseDto listMyOrders(Long userId, int page, int size) {
        if (page < 1 || size < 1) {
            throw new GlobalException(GlobalErrorCode.INVALID_PAGINATION);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("orderDate")));
        Page<Order> orderPage = orderRepository.findByUser(user, pageable);

        List<OrderSummaryDto> contents = orderPage.getContent().stream()
                .map(OrderSummaryDto::from)
                .toList();

        return new OrderListResponseDto(
                true,
                contents,
                page,
                size,
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );
    }
}
