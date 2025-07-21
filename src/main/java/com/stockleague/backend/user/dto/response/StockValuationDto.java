package com.stockleague.backend.user.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockValuationDto {

    private String ticker;
    private String stockName;
    private BigDecimal quantity;
    private BigDecimal avgBuyPrice;
    private BigDecimal currentPrice;
    private BigDecimal valuation;
    private BigDecimal profit;
    private BigDecimal profitRate;

    public static StockValuationDto of(String ticker,
                                       String stockName,
                                       BigDecimal quantity,
                                       BigDecimal avgBuyPrice,
                                       BigDecimal currentPrice) {
        BigDecimal valuation = currentPrice.multiply(quantity);
        BigDecimal buyCost = avgBuyPrice.multiply(quantity);
        BigDecimal profit = valuation.subtract(buyCost);
        BigDecimal profitRate = BigDecimal.ZERO;

        if (buyCost.compareTo(BigDecimal.ZERO) != 0) {
            profitRate = profit.divide(buyCost, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        return StockValuationDto.builder()
                .ticker(ticker)
                .stockName(stockName)
                .quantity(quantity)
                .avgBuyPrice(avgBuyPrice)
                .currentPrice(currentPrice)
                .valuation(valuation)
                .profit(profit)
                .profitRate(profitRate)
                .build();
    }
}
