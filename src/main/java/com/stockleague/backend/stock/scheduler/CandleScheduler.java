package com.stockleague.backend.stock.scheduler;

import com.stockleague.backend.stock.service.StockDailyPriceService;
import com.stockleague.backend.stock.service.StockMinutePriceService;
import com.stockleague.backend.stock.service.StockMonthlyPriceService;
import com.stockleague.backend.stock.service.StockWeeklyPriceService;
import com.stockleague.backend.stock.service.StockYearlyPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CandleScheduler {

    private final StockMinutePriceService stockMinutePriceService;
    private final StockDailyPriceService stockDailyPriceService;
    private final StockWeeklyPriceService stockWeeklyPriceService;
    private final StockMonthlyPriceService stockMonthlyPriceService;
    private final StockYearlyPriceService stockYearlyPriceService;

    private boolean isMarketOpen() {
        LocalTime now = LocalTime.now();
        LocalTime marketStart = LocalTime.of(9, 0);
        LocalTime marketEnd = LocalTime.of(15, 30);
        return !now.isBefore(marketStart) && !now.isAfter(marketEnd);
    }

    /** 1분봉: 매 분 */
    @Scheduled(cron = "0 * * * * MON-FRI")
    public void generate1Minute() {
        if (!isMarketOpen()) return;
        log.info("[1분봉] 생성 시작");
        stockMinutePriceService.aggregateAndSave(1);
    }

    /** 3분봉: 3분 간격 */
    @Scheduled(cron = "0 */3 * * * MON-FRI")
    public void generate3Minute() {
        if (!isMarketOpen()) return;
        log.info("[3분봉] 생성 시작");
        stockMinutePriceService.aggregateAndSave(3);
    }

    /** 5분봉: 5분 간격 */
    @Scheduled(cron = "0 */5 * * * MON-FRI")
    public void generate5Minute() {
        if (!isMarketOpen()) return;
        log.info("[5분봉] 생성 시작");
        stockMinutePriceService.aggregateAndSave(5);
    }

    /** 10분봉 */
    @Scheduled(cron = "0 */10 * * * MON-FRI")
    public void generate10Minute() {
        if (!isMarketOpen()) return;
        log.info("[10분봉] 생성 시작");
        stockMinutePriceService.aggregateAndSave(10);
    }

    /** 15분봉 */
    @Scheduled(cron = "0 */15 * * * MON-FRI")
    public void generate15Minute() {
        if (!isMarketOpen()) return;
        log.info("[15분봉] 생성 시작");
        stockMinutePriceService.aggregateAndSave(15);
    }

    /** 30분봉 */
    @Scheduled(cron = "0 */30 * * * MON-FRI")
    public void generate30Minute() {
        if (!isMarketOpen()) return;
        log.info("[30분봉] 생성 시작");
        stockMinutePriceService.aggregateAndSave(30);
    }

    /** 60분봉: 매 정시 */
    @Scheduled(cron = "0 0 * * * MON-FRI")
    public void generate60Minute() {
        if (!isMarketOpen()) return;
        log.info("[60분봉] 생성 시작");
        stockMinutePriceService.aggregateAndSave(60);
    }

    /** 오래된 분봉 데이터 제거 */
    @Scheduled(cron = "0 10 0 * * SAT")
    public void cleanupRedisOldPrices() {
        log.info("주말 Redis 분봉 데이터 정리 시작");
        stockMinutePriceService.removeOldRedisPricesAll();
    }

    /**
     * 매주 월요일 ~ 금요일, 15:40에 일봉 생성 시도
     * - 실제 장 마감은 15:30이므로, 그 이후 10분 이상 지난 시점에 생성
     */
    @Scheduled(cron = "0 40 15 * * MON-FRI")
    public void generateDailyCandles() {
        log.info("일봉 생성 시작");
        stockDailyPriceService.generateDailyCandles();
    }

    /**
     * 매주 금요일 16:00에 주봉 생성
     * - 한 주간(월~금)의 일봉 데이터를 기반으로 주봉 생성
     */
    @Scheduled(cron = "0 0 16 * * FRI")
    public void generateWeeklyCandles() {
        log.info("주봉 생성 시작");
        stockWeeklyPriceService.generateWeeklyCandle();
    }

    /**
     * 매월 마지막 평일 16:10에 월봉 생성
     * - 해당 월의 일봉 데이터를 기반으로 월봉 생성
     */
    @Scheduled(cron = "0 10 16 L * MON-FRI")
    public void generateMonthlyCandles() {
        log.info("월봉 생성 시작");
        stockMonthlyPriceService.generateMonthlyCandles();
    }

    /**
     * 매년 12월 31일 16:20에 연봉 생성
     * - 해당 연도의 일봉 데이터를 기반으로 연봉 생성
     */
    @Scheduled(cron = "0 20 16 31 12 *")
    public void generateYearlyCandles() {
        log.info("연봉 생성 시작");
        stockYearlyPriceService.generateYearlyCandles();
    }
}
