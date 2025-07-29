package com.stockleague.backend.user.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAssetSnapshotDto {

    private BigDecimal cashBalance;
    private BigDecimal stockValuation;
    private BigDecimal totalAsset;
    private BigDecimal totalProfit;
    private BigDecimal totalProfitRate;
    private List<StockValuationDto> stocks;

    public static UserAssetSnapshotDto from(UserAssetValuationDto liveValuation) {
        return UserAssetSnapshotDto.builder()
                .cashBalance(liveValuation.getCashBalance())
                .stockValuation(liveValuation.getStockValuation())
                .totalAsset(liveValuation.getTotalAsset())
                .totalProfit(liveValuation.getTotalProfit())
                .totalProfitRate(liveValuation.getTotalProfitRate())
                .stocks(liveValuation.getStocks())
                .build();
    }
}
