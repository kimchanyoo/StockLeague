package com.stockleague.backend.user.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserAssetValuationDto {

    private BigDecimal cashBalance;
    private BigDecimal stockValuation;
    private BigDecimal totalAsset;
    private BigDecimal totalProfit;
    private BigDecimal totalProfitRate;
    private List<StockValuationDto> stocks;

    public static UserAssetValuationDto of(BigDecimal cashBalance,
                                           List<StockValuationDto> stocks) {
        BigDecimal stockValuation = stocks.stream()
                .map(StockValuationDto::getValuation)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAsset = cashBalance.add(stockValuation);

        BigDecimal totalProfit = stocks.stream()
                .map(StockValuationDto::getProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalBuyValue = stockValuation.subtract(totalProfit);

        BigDecimal totalProfitRate = BigDecimal.ZERO;

        if (totalBuyValue.compareTo(BigDecimal.ZERO) != 0) {
            totalProfitRate = totalProfit.divide(totalBuyValue, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return UserAssetValuationDto.builder()
                .cashBalance(cashBalance)
                .stockValuation(stockValuation)
                .totalAsset(totalAsset)
                .totalProfit(totalProfit)
                .totalProfitRate(totalProfitRate)
                .stocks(stocks)
                .build();
    }
}
