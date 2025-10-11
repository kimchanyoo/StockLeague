package com.stockleague.backend.user.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class UserAssetValuationDto {

    private BigDecimal availableCash;
    private BigDecimal reservedCash;
    private BigDecimal totalCash;
    private BigDecimal stockValuation;
    private BigDecimal totalAsset;
    private BigDecimal totalProfit;
    private BigDecimal totalProfitRate;
    private List<StockValuationDto> stocks;
    private boolean marketOpen;

    public static UserAssetValuationDto of(
            BigDecimal availableCash,
            List<StockValuationDto> stocks,
            boolean isMarketOpen,
            BigDecimal reservedCash
    ) {
        if (availableCash == null) availableCash = BigDecimal.ZERO;
        if (reservedCash == null) reservedCash = BigDecimal.ZERO;
        if (stocks == null) stocks = java.util.Collections.emptyList();

        BigDecimal stockValuation = stocks.stream()
                .map(StockValuationDto::getValuation)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProfit = stocks.stream()
                .map(StockValuationDto::getProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalBuyValue = stockValuation.subtract(totalProfit);

        BigDecimal totalProfitRate = BigDecimal.ZERO;
        if (totalBuyValue.compareTo(BigDecimal.ZERO) != 0) {
            totalProfitRate = totalProfit
                    .divide(totalBuyValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        BigDecimal totalCash = availableCash.add(reservedCash);
        BigDecimal totalAsset = totalCash.add(stockValuation);

        return UserAssetValuationDto.builder()
                .availableCash(availableCash)
                .reservedCash(reservedCash)
                .totalCash(totalCash)
                .stockValuation(stockValuation)
                .totalAsset(totalAsset)
                .totalProfit(totalProfit)
                .totalProfitRate(totalProfitRate)
                .stocks(stocks)
                .marketOpen(isMarketOpen)
                .build();
    }

}
