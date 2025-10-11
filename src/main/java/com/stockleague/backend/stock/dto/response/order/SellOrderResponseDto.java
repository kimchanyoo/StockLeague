package com.stockleague.backend.stock.dto.response.order;

public record SellOrderResponseDto(
        boolean success,
        String message
) {
    public static SellOrderResponseDto from() {
        return new SellOrderResponseDto(
                true,
                "매도 주문이 정상적으로 접수되었습니다.");
    }
}
