package com.stockleague.backend.global.util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class MarketTimeUtil {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final LocalTime MARKET_OPEN = LocalTime.of(8, 59);
    private static final LocalTime MARKET_CLOSE = LocalTime.of(19, 0);

    private MarketTimeUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 현재 시각이 장 운영 시간(평일 09:00~15:00)에 해당하는지 여부를 반환합니다.
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

    /** 지금이 호가(OrderBook) 수집 허용 시간인지 (평일 && 15:00 이전) */
    public static boolean shouldCollectOrderbookNow() {
        ZonedDateTime now = ZonedDateTime.now(KST);
        if (!isWeekday(now)) return false;
        LocalTime t = now.toLocalTime();
        return !t.isBefore(MARKET_OPEN) && t.isBefore(MARKET_CLOSE);
    }

    private static boolean isWeekday(ZonedDateTime zdt) {
        DayOfWeek d = zdt.getDayOfWeek();
        return d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY;
    }

    /**
     * 장이 종료되었는지 여부 반환 (반대 조건)
     */
    public static boolean isMarketClosed() {
        return !isMarketOpen();
    }
}
