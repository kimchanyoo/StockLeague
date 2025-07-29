package com.stockleague.backend.stock.dto.response.execution;

import com.stockleague.backend.stock.domain.OrderExecution;
import com.stockleague.backend.stock.domain.OrderType;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public record OrderExecutionSummaryDto(
        Long orderExecutionId,
        String stockName,
        OrderType orderType,
        BigDecimal executedPrice,
        BigDecimal executedAmount,
        String executedAt
) {
    public static OrderExecutionSummaryDto from(OrderExecution orderExecution) {
        return new OrderExecutionSummaryDto(
                orderExecution.getId(),
                orderExecution.getOrder().getStock().getStockName(),
                orderExecution.getOrder().getOrderType(),
                orderExecution.getExecutedPrice(),
                orderExecution.getExecutedAmount(),
                orderExecution.getExecutedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
