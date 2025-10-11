package com.stockleague.backend.stock.service;

import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockDailyPrice;
import com.stockleague.backend.stock.domain.StockMonthlyPrice;
import com.stockleague.backend.stock.repository.StockDailyPriceRepository;
import com.stockleague.backend.stock.repository.StockMonthlyPriceRepository;
import com.stockleague.backend.stock.repository.StockRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockMonthlyPriceService {

    private final StockRepository stockRepository;
    private final StockDailyPriceRepository dailyRepo;
    private final StockMonthlyPriceRepository monthlyRepo;

    /**
     * 모든 종목에 대해 월봉을 생성
     */
    public void generateMonthlyCandles() {
        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.withDayOfMonth(1);
        LocalDate lastDay = today.withDayOfMonth(today.lengthOfMonth());

        int year = today.getYear();
        int month = today.getMonthValue();

        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            if (monthlyRepo.existsByStockAndYearAndMonth(stock, year, month)) {
                log.debug("[월봉 스킵] 이미 생성됨: {} - {}년 {}월", stock.getStockTicker(), year, month);
                continue;
            }

            List<StockDailyPrice> dailyList =
                    dailyRepo.findAllByStockAndDateBetweenOrderByDateAsc(stock, firstDay, lastDay);

            if (dailyList.isEmpty()) {
                log.warn("[월봉 스킵] 일봉 없음: {} - {}년 {}월", stock.getStockTicker(), year, month);
                continue;
            }

            long open = dailyList.get(0).getOpenPrice();
            long close = dailyList.get(dailyList.size() - 1).getClosePrice();
            long high = dailyList.stream().mapToLong(StockDailyPrice::getHighPrice).max().orElse(open);
            long low = dailyList.stream().mapToLong(StockDailyPrice::getLowPrice).min().orElse(open);
            long volume = dailyList.stream().mapToLong(StockDailyPrice::getVolume).sum();

            StockMonthlyPrice candle = StockMonthlyPrice.builder()
                    .stock(stock)
                    .year(year)
                    .month(month)
                    .openPrice(open)
                    .highPrice(high)
                    .lowPrice(low)
                    .closePrice(close)
                    .volume(volume)
                    .build();

            monthlyRepo.save(candle);
            log.info("[월봉 생성] 저장 완료: {} - {}년 {}월", stock.getStockTicker(), year, month);
        }
    }
}
