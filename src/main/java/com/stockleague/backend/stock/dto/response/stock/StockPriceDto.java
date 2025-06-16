package com.stockleague.backend.stock.dto.response.stock;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "단일 종목의 실시간 시세 정보")
public record StockPriceDto(

        @Schema(description = "종목코드", example = "005930")
        String ticker,

        @Schema(description = "기준일자 (yyyyMMdd)", example = "20250529")
        String date,

        @Schema(description = "시가", example = "72000")
        int openPrice,

        @Schema(description = "고가", example = "73500")
        int highPrice,

        @Schema(description = "저가", example = "71500")
        int lowPrice,

        @Schema(description = "종가 (또는 현재가)", example = "72800")
        int closePrice,

        @Schema(description = "현재가", example = "72800")
        int currentPrice,

        @Schema(description = "전일 대비 가격 차이", example = "300")
        int priceChange,

        @Schema(description = "전일 대비율", example = "2.15")
        int pricePercent,

        @Schema(description = "등락 부호 (1:상승, 2:하락, 3:보합)", example = "1")
        int changeSign,

        @Schema(description = "누적 거래량", example = "14322098")
        long accumulatedVolume
) {}
