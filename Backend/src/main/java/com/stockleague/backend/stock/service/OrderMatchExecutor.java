package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.infra.redis.AtomicOrderbookMatcher;
import com.stockleague.backend.notification.domain.NotificationType;
import com.stockleague.backend.notification.domain.TargetType;
import com.stockleague.backend.notification.dto.NotificationEvent;
import com.stockleague.backend.notification.service.NotificationService;
import com.stockleague.backend.infra.redis.OrderQueueRedisService;
import com.stockleague.backend.stock.domain.Order;
import com.stockleague.backend.stock.domain.OrderExecution;
import com.stockleague.backend.stock.domain.OrderType;
import com.stockleague.backend.stock.domain.ReservedCash;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.dto.response.stock.StockOrderBookDto;
import com.stockleague.backend.stock.repository.OrderExecutionRepository;
import com.stockleague.backend.stock.repository.OrderRepository;
import com.stockleague.backend.stock.repository.ReservedCashRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.domain.UserAsset;
import com.stockleague.backend.user.domain.UserStock;
import com.stockleague.backend.user.repository.UserStockRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderMatchExecutor {

    private final OrderRepository orderRepository;
    private final OrderExecutionRepository orderExecutionRepository;
    private final OrderQueueRedisService orderQueueRedisService;
    private final NotificationService notificationService;

    private final ReservedCashRepository reservedCashRepository;
    private final UserStockRepository userStockRepository;

    private final AtomicOrderbookMatcher matcher;

    /**
     * 매수 주문 1건 처리
     */
    @Transactional
    public BigDecimal processBuyOrder(Long orderId, String ticker) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.ORDER_NOT_FOUND));

        if (order.isCompletedOrCanceled()) return order.getRemainingAmount();

        BigDecimal beforeRemaining = order.getRemainingAmount();
        if (beforeRemaining.compareTo(BigDecimal.ZERO) <= 0) return beforeRemaining;

        AtomicOrderbookMatcher.MatchResult r =
                matcher.matchBuy(ticker, order.getOrderPrice().longValue(), beforeRemaining);

        if (!r.hasFill()) {
            return order.getRemainingAmount();
        }

        List<OrderExecution> executions = new ArrayList<>();
        BigDecimal totalExecutedVolume = BigDecimal.ZERO;
        BigDecimal totalExecutedVal = BigDecimal.ZERO;

        for (AtomicOrderbookMatcher.Fill f : r.getMatches()) {
            BigDecimal p = f.priceAsBigDecimal();
            BigDecimal q = f.getVolume();
            if (q == null || q.compareTo(BigDecimal.ZERO) <= 0) continue;

            executions.add(OrderExecution.builder()
                    .order(order)
                    .executedAmount(q)
                    .executedPrice(p)
                    .build());

            totalExecutedVolume = totalExecutedVolume.add(q);
            totalExecutedVal = totalExecutedVal.add(p.multiply(q));
        }

        if (executions.isEmpty()) return order.getRemainingAmount();

        orderExecutionRepository.saveAll(executions);

        order.applyExecutionDelta(totalExecutedVolume, totalExecutedVal);
        orderRepository.save(order);

        applyBuyStock(order.getUser(), order.getStock(), totalExecutedVolume,
                avgPrice(totalExecutedVal, totalExecutedVolume));

        BigDecimal afterRemaining = order.getRemainingAmount();

        if (beforeRemaining.compareTo(afterRemaining) > 0 && afterRemaining.compareTo(BigDecimal.ZERO) > 0) {
            notifyAfterCommit(
                    order.getUser().getId(),
                    NotificationType.TRADE_PARTIALLY_EXECUTED,
                    order.getId(),
                    String.format("매수 부분 체결: %s (%s주, 평균가 %s)",
                            order.getStock().getStockTicker(),
                            totalExecutedVolume.stripTrailingZeros().toPlainString(),
                            order.getAverageExecutedPrice())
            );
        }

        if (order.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            finalizeReservedCash(order, totalExecutedVal);
            orderQueueRedisService.removeOrderFromQueue(OrderType.BUY, ticker, order.getId());

            notifyAfterCommit(
                    order.getUser().getId(),
                    NotificationType.TRADE_EXECUTED,
                    order.getId(),
                    String.format("매수 최종 체결: %s (총 %s주, 평균가 %s)",
                            order.getStock().getStockTicker(),
                            order.getOrderAmount().stripTrailingZeros().toPlainString(),
                            order.getAverageExecutedPrice())
            );
        }

        return order.getRemainingAmount();
    }

    /**
     * 매도 주문 1건 처리
     */
    @Transactional
    public BigDecimal processSellOrder(Long orderId, String ticker) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.ORDER_NOT_FOUND));

        if (order.isCompletedOrCanceled()) return order.getRemainingAmount();

        BigDecimal beforeRemaining = order.getRemainingAmount();
        if (beforeRemaining.compareTo(BigDecimal.ZERO) <= 0) return beforeRemaining;

        AtomicOrderbookMatcher.MatchResult r =
                matcher.matchSell(ticker, order.getOrderPrice().longValue(), beforeRemaining);

        if (!r.hasFill()) {
            return order.getRemainingAmount();
        }

        List<OrderExecution> executions = new ArrayList<>();
        BigDecimal totalExecutedVolume = BigDecimal.ZERO;
        BigDecimal totalExecutedVal = BigDecimal.ZERO;

        for (AtomicOrderbookMatcher.Fill f : r.getMatches()) {
            BigDecimal p = f.priceAsBigDecimal();
            BigDecimal q = f.getVolume();
            if (q == null || q.compareTo(BigDecimal.ZERO) <= 0) continue;

            executions.add(OrderExecution.builder()
                    .order(order)
                    .executedAmount(q)
                    .executedPrice(p)
                    .build());

            totalExecutedVolume = totalExecutedVolume.add(q);
            totalExecutedVal = totalExecutedVal.add(p.multiply(q));
        }

        if (executions.isEmpty()) return order.getRemainingAmount();

        orderExecutionRepository.saveAll(executions);
        order.applyExecutionDelta(totalExecutedVolume, totalExecutedVal);
        orderRepository.save(order);

        finalizeSellStock(order.getUser(), order.getStock(), totalExecutedVolume);
        applySellRevenue(order.getUser(), totalExecutedVal);

        BigDecimal afterRemaining = order.getRemainingAmount();

        if (beforeRemaining.compareTo(afterRemaining) > 0 && afterRemaining.compareTo(BigDecimal.ZERO) > 0) {
            notifyAfterCommit(
                    order.getUser().getId(),
                    NotificationType.TRADE_PARTIALLY_EXECUTED,
                    order.getId(),
                    String.format("매도 부분 체결: %s (%s주, 평균가 %s)",
                            order.getStock().getStockTicker(),
                            totalExecutedVolume.stripTrailingZeros().toPlainString(),
                            order.getAverageExecutedPrice())
            );
        }

        if (order.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            orderQueueRedisService.removeOrderFromQueue(OrderType.SELL, ticker, order.getId());

            notifyAfterCommit(
                    order.getUser().getId(),
                    NotificationType.TRADE_EXECUTED,
                    order.getId(),
                    String.format("매도 최종 체결: %s (총 %s주, 평균가 %s)",
                            order.getStock().getStockTicker(),
                            order.getOrderAmount().stripTrailingZeros().toPlainString(),
                            order.getAverageExecutedPrice())
            );
        }

        return order.getRemainingAmount();
    }

    /**
     * 체결 금액 총합과 체결 수량으로 평균 체결 단가를 계산
     * @param totalVal 체결 금액 총액
     * @param volume 체결 수량 총합
     * @return 평균 체결 단가
     */
    private BigDecimal avgPrice(BigDecimal totalVal, BigDecimal volume) {
        if (volume.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return totalVal.divide(volume, 2, RoundingMode.HALF_UP);
    }

    /**
     * 매수 체결 시, 유저의 보유 주식 정보를 갱신합니다.
     * <ul>
     *     <li>UserStock이 없으면 새로 생성하며 평균 단가는 체결 단가로 설정됩니다.</li>
     *     <li>기존에 보유하고 있으면 수량과 평균 단가를 갱신합니다.</li>
     *     <li>평균 단가는 가중 평균 방식으로 계산됩니다.</li>
     * </ul>
     *
     * @param user          사용자
     * @param stock         종목
     * @param amount        매수 체결 수량
     * @param executedPrice 매수 체결 단가
     */
    private void applyBuyStock(User user, Stock stock, BigDecimal amount, BigDecimal executedPrice) {
        UserStock us = userStockRepository.findByUserAndStock(user, stock).orElse(null);
        if (us == null) {
            us = UserStock.builder()
                    .user(user)
                    .stock(stock)
                    .quantity(amount)
                    .lockedQuantity(BigDecimal.ZERO)
                    .avgBuyPrice(executedPrice)
                    .build();
        } else {
            BigDecimal currentVolume = us.getQuantity();
            BigDecimal currentAvg = us.getAvgBuyPrice();
            BigDecimal totalVolume = currentVolume.add(amount);
            BigDecimal totalVal = currentAvg.multiply(currentVolume).add(executedPrice.multiply(amount));
            BigDecimal newAvg = totalVal.divide(totalVolume, 2, RoundingMode.HALF_UP);

            us.increaseQuantity(amount);
            us.setAvgBuyPrice(newAvg);
        }
        userStockRepository.save(us);
    }

    /**
     * 매수 주문 체결 완료 후 예약된 현금을 정산하고 남은 금액을 환불합니다.
     * <ul>
     *     <li>실제 체결 금액이 예약 금액보다 적은 경우, 차액을 유저 자산에 환불합니다.</li>
     *     <li>환불 여부는 ReservedCash 엔티티의 refunded 필드를 true로 설정합니다.</li>
     * </ul>
     *
     * @param order      체결된 주문
     * @param actualCost 실제 체결 금액
     * @throws GlobalException RESERVED_CASH_NOT_FOUND - 예약 현금 정보가 없는 경우
     */
    private void finalizeReservedCash(Order order, BigDecimal actualCost) {
        ReservedCash rc = reservedCashRepository.findByOrder(order)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.RESERVED_CASH_NOT_FOUND));

        BigDecimal reserved = rc.getReservedAmount();
        BigDecimal refund = reserved.subtract(actualCost);

        if (refund.compareTo(BigDecimal.ZERO) > 0) {
            UserAsset asset = order.getUser().getUserAsset();
            if (asset == null) throw new GlobalException(GlobalErrorCode.USER_ASSET_NOT_FOUND);
            asset.addCash(refund);
        }
        rc.markAsRefunded(refund);
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
        UserStock us = userStockRepository.findByUserAndStock(user, stock)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_STOCK_NOT_FOUND));

        BigDecimal beforeLocked = us.getLockedQuantity();
        us.executeSell(executedAmount);
        userStockRepository.save(us);

        log.info("[UserStock][SELL] userId={}, ticker={}, locked {} -> {} (exec={})",
                user.getId(),
                stock.getStockTicker(),
                beforeLocked.stripTrailingZeros().toPlainString(),
                us.getLockedQuantity().stripTrailingZeros().toPlainString(),
                executedAmount.stripTrailingZeros().toPlainString());
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
        if (asset == null) throw new GlobalException(GlobalErrorCode.USER_ASSET_NOT_FOUND);
        asset.addCash(revenue);
    }

    private void notifyAfterCommit(Long userId, NotificationType type, Long targetId, String message) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {
                    notificationService.notify(
                            new NotificationEvent(userId, type, TargetType.TRADE, targetId),
                            message
                    );
                }
            });
        } else {
            // 트랜잭션 밖이라면 즉시 전송
            notificationService.notify(
                    new NotificationEvent(userId, type, TargetType.TRADE, targetId),
                    message
            );
        }
    }
}
