package com.stockleague.backend.stock.service;

import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockDailyPrice;
import com.stockleague.backend.stock.domain.StockYearlyPrice;
import com.stockleague.backend.stock.repository.StockDailyPriceRepository;
import com.stockleague.backend.stock.repository.StockRepository;
import com.stockleague.backend.stock.repository.StockYearlyPriceRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockYearlyPriceService {

    private final StockRepository stockRepository;
    private final StockDailyPriceRepository dailyRepo;
    private final StockYearlyPriceRepository yearlyRepo;

    /**
     * 모든 종목에 대해 연봉 생성
     */
    public void generateYearlyCandles() {
        int year = LocalDate.now().getYear();

        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            if (yearlyRepo.existsByStockAndYear(stock, year)) {
                log.debug("[연봉 생성] 이미 존재: {} - {}년", stock.getStockTicker(), year);
                continue;
            }

            LocalDate start = LocalDate.of(year, 1, 1);
            LocalDate end = LocalDate.of(year, 12, 31);
            List<StockDailyPrice> dailyList =
                    dailyRepo.findAllByStockAndDateBetweenOrderByDateAsc(stock, start, end);

            if (dailyList.isEmpty()) {
                log.warn("[연봉 생성] 일봉 없음: {} - {}년", stock.getStockTicker(), year);
                continue;
            }

            long open = dailyList.get(0).getOpenPrice();
            long close = dailyList.get(dailyList.size() - 1).getClosePrice();
            long high = dailyList.stream().mapToLong(StockDailyPrice::getHighPrice).max().orElse(open);
            long low = dailyList.stream().mapToLong(StockDailyPrice::getLowPrice).min().orElse(open);
            long volume = dailyList.stream().mapToLong(StockDailyPrice::getVolume).sum();

            StockYearlyPrice candle = StockYearlyPrice.builder()
                    .stock(stock)
                    .year(year)
                    .openPrice(open)
                    .highPrice(high)
                    .lowPrice(low)
                    .closePrice(close)
                    .volume(volume)
                    .build();

            yearlyRepo.save(candle);
            log.info("[연봉 생성] 저장 완료: {} - {}년", stock.getStockTicker(), year);
        }
    }
}
