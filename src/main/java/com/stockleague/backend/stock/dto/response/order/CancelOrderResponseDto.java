package com.stockleague.backend.stock.dto.response.order;

public record CancelOrderResponseDto(
        boolean success,
        String message
) {
    public static CancelOrderResponseDto from() {
        return new CancelOrderResponseDto(
                true,
                "주문 취소가 완료되었습니다."
        );
    }
}
