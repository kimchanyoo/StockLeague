package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.infra.redis.StockOrderBookRedisService;
import com.stockleague.backend.stock.domain.Order;
import com.stockleague.backend.stock.domain.OrderExecution;
import com.stockleague.backend.stock.domain.OrderStatus;
import com.stockleague.backend.stock.domain.ReservedCash;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.dto.response.execution.ExecutionHistoryResponseDto;
import com.stockleague.backend.stock.dto.response.execution.OrderExecutionListResponseDto;
import com.stockleague.backend.stock.dto.response.execution.OrderExecutionSummaryDto;
import com.stockleague.backend.stock.dto.response.execution.UnexecutedOrderListResponseDto;
import com.stockleague.backend.stock.dto.response.execution.UnexecutedOrderSummaryDto;
import com.stockleague.backend.stock.dto.response.order.OrderSummaryDto;
import com.stockleague.backend.stock.dto.response.stock.StockOrderBookDto;
import com.stockleague.backend.stock.repository.OrderExecutionRepository;
import com.stockleague.backend.stock.repository.OrderRepository;
import com.stockleague.backend.stock.repository.ReservedCashRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderExecutionService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ReservedCashRepository reservedCashRepository;
    private final OrderExecutionRepository orderExecutionRepository;
    private final UserStockRepository userStockRepository;
    private final StockOrderBookRedisService stockOrderBookRedisService;

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
    public BigDecimal tryImmediateBuyExecution(Order order) {
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
    public BigDecimal tryImmediateSellExecution(Order order) {
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

        reservedCash.markAsRefunded(refund);
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
     * 사용자의 개별 주문 상세 정보를 조회합니다.
     * <p>
     * 요청한 사용자 본인의 주문에 대해서만 상세 정보를 반환하며,
     * 주문이 존재하지 않거나 권한이 없는 경우 예외를 발생시킵니다.
     * </p>
     *
     * @param userId 조회 대상 사용자 ID
     * @param orderId 조회 대상 주문 ID
     * @return 사용자의 주문 내역 리스트와 페이지 정보가 포함된 응답 DTO {@link OrderSummaryDto}
     * @throws GlobalException 다음과 같은 예외가 발생할 수 있습니다:
     *          <ul>
     *              <li>{@code ORDER_NOT_FOUND} - 주문이 존재하지 않는 경우</li>
     *              <li>{@code UNAUTHORIZED_ORDER_ACCESS} - 다른 사용자의 주문을 취소하려는 경우</li>
     *          </ul>
     */
    @Transactional(readOnly = true)
    public OrderExecutionListResponseDto getOrderExecutions(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userId)) {
            throw new GlobalException(GlobalErrorCode.UNAUTHORIZED_ORDER_ACCESS);
        }

        List<OrderExecution> executions = orderExecutionRepository.findByOrderOrderByExecutedAtDesc(order);

        List<OrderExecutionSummaryDto> response = executions.stream()
                .map(OrderExecutionSummaryDto::from)
                .toList();

        return OrderExecutionListResponseDto.from(response);
    }

    /**
     * 사용자의 전체 체결 내역을 페이지 단위로 조회합니다.
     * <p>
     * 체결 내역은 체결 시각(executedAt) 기준 내림차순으로 정렬되며,
     * 매수(BUY), 매도(SELL) 구분 없이 모든 체결 내역이 포함됩니다.
     * </p>
     *
     * @param userId 조회 대상 사용자 ID
     * @param page   조회할 페이지 번호 (1부터 시작)
     * @param size   페이지당 항목 수
     * @return 사용자의 체결 내역 리스트와 페이지 정보가 포함된 응답 DTO {@link ExecutionHistoryResponseDto}
     * @throws GlobalException 다음과 같은 예외가 발생할 수 있습니다:
     *          <ul>
     *              <li>{@code INVALID_PAGINATION} - 페이지 번호 또는 크기가 1 미만인 경우</li>
     *              <li>{@code USER_NOT_FOUND} - 사용자가 존재하지 않는 경우</li>
     *          </ul>
     */
    @Transactional(readOnly = true)
    public ExecutionHistoryResponseDto listUserExecutions(Long userId, int page, int size) {
        if (page < 1 || size < 1) {
            throw new GlobalException(GlobalErrorCode.INVALID_PAGINATION);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("executedAt")));

        Page<OrderExecution> executionPage = orderExecutionRepository.findByOrderUser(user, pageable);

        List<OrderExecutionSummaryDto> contents = executionPage.getContent().stream()
                .map(OrderExecutionSummaryDto::from)
                .toList();

        return new ExecutionHistoryResponseDto(
                true,
                contents,
                page,
                size,
                executionPage.getTotalElements(),
                executionPage.getTotalPages()
        );
    }

    /**
     * 사용자의 전체 미체결 주문 내역을 페이지 단위로 조회합니다.
     * <p>
     * 미체결 주문은 상태가 {@code WAITING} 또는 {@code PARTIALLY_EXECUTED}인 주문으로 정의됩니다.
     * 결과는 주문 생성 시각({@code createdAt}) 기준 내림차순으로 정렬되어 반환됩니다.
     * 매수(BUY), 매도(SELL) 구분 없이 모든 미체결 주문이 포함됩니다.
     * </p>
     *
     * @param userId 조회 대상 사용자 ID
     * @param page   조회할 페이지 번호 (1부터 시작)
     * @param size   페이지당 항목 수
     * @return 사용자의 미체결 주문 리스트와 페이지 정보가 포함된 응답 DTO {@link UnexecutedOrderListResponseDto}
     * @throws GlobalException 다음과 같은 예외가 발생할 수 있습니다:
     *          <ul>
     *              <li>{@code INVALID_PAGINATION} - 페이지 번호 또는 크기가 1 미만인 경우</li>
     *              <li>{@code USER_NOT_FOUND} - 사용자가 존재하지 않는 경우</li>
     *          </ul>
     */
    @Transactional(readOnly = true)
    public UnexecutedOrderListResponseDto listUnexecutedOrders(Long userId, int page, int size) {
        if (page < 1 || size < 1) {
            throw new GlobalException(GlobalErrorCode.INVALID_PAGINATION);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        List<OrderStatus> unexecutedStatuses = List.of(
                OrderStatus.WAITING,
                OrderStatus.PARTIALLY_EXECUTED
        );

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<Order> orderPage = orderRepository.findByUserAndStatusIn(user, unexecutedStatuses, pageable);

        List<UnexecutedOrderSummaryDto> contents = orderPage.getContent().stream()
                .map(UnexecutedOrderSummaryDto::from)
                .toList();

        return new UnexecutedOrderListResponseDto(
                true,
                contents,
                page,
                size,
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );
    }
}
