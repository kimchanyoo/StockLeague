package com.stockleague.backend.stock.dto.response.execution;

import com.stockleague.backend.stock.domain.Order;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public record UnexecutedOrderSummaryDto(
        Long orderId,
        String stockName,
        String stockTicker,
        String orderType,
        BigDecimal orderPrice,
        BigDecimal orderAmount,
        BigDecimal remainingAmount,
        String status,
        String createdAt
) {
    public static UnexecutedOrderSummaryDto from(Order order) {
        return new UnexecutedOrderSummaryDto(
                order.getId(),
                order.getStock().getStockName(),
                order.getStock().getStockTicker(),
                order.getOrderType().name(),
                order.getOrderPrice(),
                order.getOrderAmount(),
                order.getRemainingAmount(),
                order.getStatus().name(),
                order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
