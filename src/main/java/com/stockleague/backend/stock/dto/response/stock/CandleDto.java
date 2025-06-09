package com.stockleague.backend.stock.dto.response.stock;

import com.stockleague.backend.stock.domain.StockDailyPrice;
import com.stockleague.backend.stock.domain.StockMonthlyPrice;
import com.stockleague.backend.stock.domain.StockWeeklyPrice;
import com.stockleague.backend.stock.domain.StockYearlyPrice;
import io.swagger.v3.oas.annotations.media.Schema;

public record CandleDto(
        @Schema(description = "종목 티커", example = "005930")
        String ticker,

        @Schema(description = "봉 데이터의 날짜/시간", example = "2023-01-01")
        String dateTime,

        @Schema(description = "시가", example = "70000")
        Long openPrice,

        @Schema(description = "고가", example = "80000")
        Long highPrice,

        @Schema(description = "저가", example = "65000")
        Long lowPrice,

        @Schema(description = "종가", example = "75000")
        Long closePrice,

        @Schema(description = "거래량", example = "120000000")
        Long volume
) {
    public static CandleDto from(StockYearlyPrice entity) {
        return new CandleDto(
                entity.getStock().getStockTicker(),
                entity.getYear() + "-01-01",
                entity.getOpenPrice(),
                entity.getHighPrice(),
                entity.getLowPrice(),
                entity.getClosePrice(),
                entity.getVolume()
        );
    }

    public static CandleDto from(StockMonthlyPrice entity) {
        return new CandleDto(
                entity.getStock().getStockTicker(),
                String.format("%04d-%02d-01", entity.getYear(), entity.getMonth()),
                entity.getOpenPrice(),
                entity.getHighPrice(),
                entity.getLowPrice(),
                entity.getClosePrice(),
                entity.getVolume()
        );
    }

    public static CandleDto from(StockWeeklyPrice entity) {
        return new CandleDto(
                entity.getStock().getStockTicker(),
                String.format("%04d-W%02d", entity.getYear(), entity.getWeek()),
                entity.getOpenPrice(),
                entity.getHighPrice(),
                entity.getLowPrice(),
                entity.getClosePrice(),
                entity.getVolume()
        );
    }

    public static CandleDto from(StockDailyPrice entity) {
        return new CandleDto(
                entity.getStock().getStockTicker(),
                entity.getDate().toString(),
                entity.getOpenPrice(),
                entity.getHighPrice(),
                entity.getLowPrice(),
                entity.getClosePrice(),
                entity.getVolume()
        );
    }
}
