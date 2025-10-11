package com.stockleague.backend.stock.dto.response.order;

import com.stockleague.backend.stock.domain.Order;
import com.stockleague.backend.stock.domain.OrderStatus;
import com.stockleague.backend.stock.domain.OrderType;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public record OrderSummaryDto (
        Long orderId,
        String stockTicker,
        String stockName,
        OrderType orderType,
        OrderStatus orderStatus,
        BigDecimal orderPrice,
        BigDecimal orderAmount,
        BigDecimal executedAmount,
        BigDecimal remainingAmount,
        BigDecimal averageExecutedPrice,
        String createdAt
) {
    public static OrderSummaryDto from(Order order) {
        return new OrderSummaryDto(
                order.getId(),
                order.getStock().getStockTicker(),
                order.getStock().getStockName(),
                order.getOrderType(),
                order.getStatus(),
                order.getOrderPrice(),
                order.getOrderAmount(),
                order.getExecutedAmount(),
                order.getRemainingAmount(),
                order.getAverageExecutedPrice(),
                order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
