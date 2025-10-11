package com.stockleague.backend.stock.dto.response.execution;

import java.util.List;

public record OrderExecutionListResponseDto(
        boolean success,
        List<OrderExecutionSummaryDto> contents
) {
    public static OrderExecutionListResponseDto from(List<OrderExecutionSummaryDto> executions) {
        return new OrderExecutionListResponseDto(
                true,
                executions);
    }
}
