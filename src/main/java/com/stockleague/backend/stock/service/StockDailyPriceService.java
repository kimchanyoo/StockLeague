package com.stockleague.backend.stock.service;

import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockDailyPrice;
import com.stockleague.backend.stock.domain.StockMinutePrice;
import com.stockleague.backend.stock.repository.StockDailyPriceRepository;
import com.stockleague.backend.stock.repository.StockMinutePriceRepository;
import com.stockleague.backend.stock.repository.StockRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockDailyPriceService {

    private final StockRepository stockRepository;
    private final StockMinutePriceRepository minuteRepo;
    private final StockDailyPriceRepository dailyRepo;

    /**
     * 모든 종목에 대해 당일 일봉을 생성
     */
    public void generateDailyCandles() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atTime(9, 0);
        LocalDateTime end = today.atTime(15, 30);

        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            if (dailyRepo.existsByStockAndDate(stock, today)) {
                continue;
            }

            List<StockMinutePrice> minuteCandles =
                    minuteRepo.findAllByStockAndCandleTimeBetweenOrderByCandleTimeAsc(stock, start, end);

            if (minuteCandles.isEmpty()) {
                log.warn("[일봉 생성] 분봉 없음: {} {}", stock.getStockTicker(), today);
                continue;
            }

            long open = minuteCandles.get(0).getOpenPrice();
            long close = minuteCandles.get(minuteCandles.size() - 1).getClosePrice();
            long high = minuteCandles.stream().mapToLong(StockMinutePrice::getHighPrice).max().orElse(open);
            long low = minuteCandles.stream().mapToLong(StockMinutePrice::getLowPrice).min().orElse(open);
            long volume = minuteCandles.stream().mapToLong(StockMinutePrice::getVolume).sum();

            StockDailyPrice daily = StockDailyPrice.builder()
                    .stock(stock)
                    .date(today)
                    .openPrice(open)
                    .closePrice(close)
                    .highPrice(high)
                    .lowPrice(low)
                    .volume(volume)
                    .build();

            dailyRepo.save(daily);
            log.info("[일봉 생성] 저장 완료: {} {}", stock.getStockTicker(), today);
        }
    }
}
