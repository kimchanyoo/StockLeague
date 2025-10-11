package com.stockleague.backend.stock.dto.response.order;

public record BuyOrderResponseDto(
        boolean success,
        String message
) {
    public static BuyOrderResponseDto from() {
        return new BuyOrderResponseDto(
                true,
                "매수 주문이 정상적으로 접수되었습니다.");
    }
}
