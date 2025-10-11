package com.stockleague.backend.stock.domain;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import java.util.Arrays;

public enum MarketType {
    KOSPI,
    KOSDAQ;

    public static MarketType from(String marketType) {
        return Arrays.stream(MarketType.values())
                .filter(type -> type.name().equalsIgnoreCase(marketType))
                .findFirst()
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.INVALID_MARKET_TYPE));
    }
}
