package com.stockleague.backend.stock.dto.response.stock;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;


@Schema(description = "단일 종목의 실시간 호가 정보")
public record StockOrderBookDto(

        @Schema(description = "종목코드", example = "005930")
        String ticker,

        @Schema(description = "매도 호가 가격 (1~10단계)", example = "[72800, 72900, ..., 73700]")
        long[] askPrices,

        @Schema(description = "매도 호가 잔량 (1~10단계)", example = "[1020, 845, ..., 230]")
        long[] askVolumes,

        @Schema(description = "매수 호가 가격 (1~10단계)", example = "[72700, 72600, ..., 71800]")
        long[] bidPrices,

        @Schema(description = "매수 호가 잔량 (1~10단계)", example = "[990, 1085, ..., 315]")
        long[] bidVolumes,

        @Schema(description = "호가 수신 시각", example = "2025-06-25T09:15:30")
        LocalDateTime timestamp
) {
}
