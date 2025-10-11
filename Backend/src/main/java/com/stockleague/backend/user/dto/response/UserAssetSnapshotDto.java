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

    private BigDecimal availableCash;
    private BigDecimal reservedCash;
    private BigDecimal totalCash;
    private BigDecimal stockValuation;
    private BigDecimal totalAsset;
    private BigDecimal totalProfit;
    private BigDecimal totalProfitRate;
    private List<StockValuationDto> stocks;

    public static UserAssetSnapshotDto from(UserAssetValuationDto dto) {
        return UserAssetSnapshotDto.builder()
                .availableCash(dto.getAvailableCash())
                .reservedCash(dto.getReservedCash())
                .totalCash(dto.getTotalCash())
                .stockValuation(dto.getStockValuation())
                .totalAsset(dto.getTotalAsset())
                .totalProfit(dto.getTotalProfit())
                .totalProfitRate(dto.getTotalProfitRate())
                .stocks(dto.getStocks())
                .build();
    }
}
