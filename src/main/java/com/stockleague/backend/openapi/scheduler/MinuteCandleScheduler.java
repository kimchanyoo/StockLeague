package com.stockleague.backend.openapi.scheduler;

import com.stockleague.backend.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinuteCandleScheduler {

    private final StockService stockService;

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
        stockService.aggregateAndSave(1);
    }

    /** 3분봉: 3분 간격 */
    @Scheduled(cron = "0 */3 * * * MON-FRI")
    public void generate3Minute() {
        if (!isMarketOpen()) return;
        log.info("[3분봉] 생성 시작");
        stockService.aggregateAndSave(3);
    }

    /** 5분봉: 5분 간격 */
    @Scheduled(cron = "0 */5 * * * MON-FRI")
    public void generate5Minute() {
        if (!isMarketOpen()) return;
        log.info("[5분봉] 생성 시작");
        stockService.aggregateAndSave(5);
    }

    /** 10분봉 */
    @Scheduled(cron = "0 */10 * * * MON-FRI")
    public void generate10Minute() {
        if (!isMarketOpen()) return;
        log.info("[10분봉] 생성 시작");
        stockService.aggregateAndSave(10);
    }

    /** 15분봉 */
    @Scheduled(cron = "0 */15 * * * MON-FRI")
    public void generate15Minute() {
        if (!isMarketOpen()) return;
        log.info("[15분봉] 생성 시작");
        stockService.aggregateAndSave(15);
    }

    /** 30분봉 */
    @Scheduled(cron = "0 */30 * * * MON-FRI")
    public void generate30Minute() {
        if (!isMarketOpen()) return;
        log.info("[30분봉] 생성 시작");
        stockService.aggregateAndSave(30);
    }

    /** 60분봉: 매 정시 */
    @Scheduled(cron = "0 0 * * * MON-FRI")
    public void generate60Minute() {
        if (!isMarketOpen()) return;
        log.info("[60분봉] 생성 시작");
        stockService.aggregateAndSave(60);
    }
}
