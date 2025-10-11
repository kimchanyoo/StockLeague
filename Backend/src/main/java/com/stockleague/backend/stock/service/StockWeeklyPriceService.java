package com.stockleague.backend.stock.service;

import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockDailyPrice;
import com.stockleague.backend.stock.domain.StockWeeklyPrice;
import com.stockleague.backend.stock.repository.StockDailyPriceRepository;
import com.stockleague.backend.stock.repository.StockRepository;
import com.stockleague.backend.stock.repository.StockWeeklyPriceRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockWeeklyPriceService {

    private final StockRepository stockRepository;
    private final StockDailyPriceRepository dailyRepo;
    private final StockWeeklyPriceRepository weeklyRepo;

    public void generateWeeklyCandle() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate friday = today.with(DayOfWeek.FRIDAY);

        int year = monday.getYear();
        int week = monday.get(WeekFields.ISO.weekOfWeekBasedYear());

        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            if (weeklyRepo.existsByStockAndYearAndWeek(stock, year, week)) {
                log.debug("[주봉 스킵] 이미 생성됨: {} - {}년 {}주차", stock.getStockTicker(), year, week);
                continue;
            }

            List<StockDailyPrice> dailyList =
                    dailyRepo.findAllByStockAndDateBetweenOrderByDateAsc(stock, monday, friday);
            if (dailyList.isEmpty()) {
                log.warn("[주봉 스킵] 일봉 없음: {} - {}년 {}주차", stock.getStockTicker(), year, week);
                continue;
            }

            long open = dailyList.get(0).getOpenPrice();
            long close = dailyList.get(dailyList.size() - 1).getClosePrice();
            long high = dailyList.stream().mapToLong(StockDailyPrice::getHighPrice).max().orElse(open);
            long low = dailyList.stream().mapToLong(StockDailyPrice::getLowPrice).min().orElse(open);
            long volume = dailyList.stream().mapToLong(StockDailyPrice::getVolume).sum();

            StockWeeklyPrice candle = StockWeeklyPrice.builder()
                    .stock(stock)
                    .year(year)
                    .week(week)
                    .openPrice(open)
                    .highPrice(high)
                    .lowPrice(low)
                    .closePrice(close)
                    .volume(volume)
                    .build();

            weeklyRepo.save(candle);
            log.info("[주봉 생성] 저장 완료: {} - {}년 {}주차", stock.getStockTicker(), year, week);
        }
    }
}
