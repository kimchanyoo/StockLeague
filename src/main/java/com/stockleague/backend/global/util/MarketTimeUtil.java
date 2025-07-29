package com.stockleague.backend.global.util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class MarketTimeUtil {

    private static final LocalTime MARKET_OPEN = LocalTime.of(9, 0);
    private static final LocalTime MARKET_CLOSE = LocalTime.of(15, 30);

    private MarketTimeUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 현재 시각이 장 운영 시간(평일 09:00~15:30)에 해당하는지 여부를 반환합니다.
     *
     * @return true: 장중, false: 장외
     */
    public static boolean isMarketOpen() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek day = now.getDayOfWeek();

        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return false;
        }

        LocalTime time = now.toLocalTime();
        return !time.isBefore(MARKET_OPEN) && !time.isAfter(MARKET_CLOSE);
    }

    /**
     * 장이 종료되었는지 여부 반환 (반대 조건)
     */
    public static boolean isMarketClosed() {
        return !isMarketOpen();
    }
}
