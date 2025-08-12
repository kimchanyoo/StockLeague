package com.stockleague.backend.stock.service;

import com.stockleague.backend.infra.redis.StockPriceRedisService;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockMinutePrice;
import com.stockleague.backend.stock.dto.response.stock.StockPriceDto;
import com.stockleague.backend.stock.repository.StockMinutePriceRepository;
import com.stockleague.backend.stock.repository.StockRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockMinutePriceService {

    private final StockPriceRedisService redisService;
    private final StockRepository stockRepository;
    private final StockMinutePriceRepository minuteRepo;

    /**
     * 모든 종목에 대해 주어진 간격의 분봉(OHLCV) 생성
     * @param interval 분봉 간격(분)
     */
    public void aggregateAndSave(int interval) {
        List<Stock> stocks = stockRepository.findAll();
        for (Stock stock : stocks) {
            generateMinuteCandle(stock, interval);
        }
    }

    /**
     * 단일 종목에 대해 특정 분봉 간격(interval)의 OHLCV 데이터 생성/저장
     */
    private void generateMinuteCandle(Stock stock, int interval) {
        String ticker = stock.getStockTicker();

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul")).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime to = now;
        LocalDateTime from = to.minusMinutes(interval);

        List<StockPriceDto> prices = redisService.findBetween(ticker, from, to);
        if (prices.isEmpty()) {
            log.warn("[분봉 생성] Redis 시세 없음 - {} {}분 {}", ticker, interval, from);
            return;
        }

        prices.sort(Comparator.comparing(StockPriceDto::datetime));

        long open = prices.get(0).currentPrice();
        long close = prices.get(prices.size() - 1).currentPrice();
        long high = prices.stream().mapToLong(StockPriceDto::currentPrice).max().orElse(open);
        long low  = prices.stream().mapToLong(StockPriceDto::currentPrice).min().orElse(open);

        Long startCum = prices.get(0).accumulatedVolume();
        Long endCum   = prices.get(prices.size() - 1).accumulatedVolume();

        if (startCum == null || endCum == null) {
            log.warn("[분봉 생성] 누적 거래량 누락: startCum={}, endCum={}, {} {}분 {}",
                    startCum, endCum, ticker, interval, from);
            return;
        }

        long volume = (endCum >= startCum) ? (endCum - startCum) : endCum;
        if (volume < 0) volume = 0;

        boolean exists = minuteRepo.existsByStockAndIntervalAndCandleTime(stock, interval, from);
        if (exists) {
            log.debug("[분봉 생성] 중복 - 저장 생략: {} {}분 {}", ticker, interval, from);
            return;
        }

        StockMinutePrice candle = StockMinutePrice.builder()
                .stock(stock)
                .interval(interval)
                .candleTime(from)
                .openPrice(open)
                .highPrice(high)
                .lowPrice(low)
                .closePrice(close)
                .volume(volume)
                .build();

        try {
            minuteRepo.save(candle);
        } catch (Exception e) {
            log.error("[분봉 생성] 저장 실패: {} {}분 {}, 이유: {}", ticker, interval, from, e.getMessage(), e);
        }
    }

    /**
     * Redis ZSET의 오래된 데이터 정리
     */
    public void removeOldRedisPricesAll() {
        List<Stock> stocks = stockRepository.findAll();
        for (Stock stock : stocks) {
            redisService.removeOldPrices(stock.getStockTicker());
        }
    }
}
