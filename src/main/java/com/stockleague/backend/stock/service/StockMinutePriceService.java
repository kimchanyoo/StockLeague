package com.stockleague.backend.stock.service;

import com.stockleague.backend.infra.redis.StockPriceRedisService;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockMinutePrice;
import com.stockleague.backend.stock.dto.response.stock.StockPriceDto;
import com.stockleague.backend.stock.repository.StockMinutePriceRepository;
import com.stockleague.backend.stock.repository.StockRepository;
import java.time.LocalDateTime;
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
     * Redis에서 종목별 시세 데이터를 조회하여 주어진 간격의 분봉(OHLCV) 데이터를 생성하고 저장
     * <p>테스트용으로 삼성전자(005930) 종목만 처리</p>
     *
     * @param interval 분봉 간격 (단위: 분)
     */
    public void aggregateAndSave(int interval) {
        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            if (!stock.getStockTicker().equals("005930")) {
                continue;
            }

            generateMinuteCandle(stock, interval);
        }
    }

    /**
     * 단일 종목에 대해 특정 분봉 간격(interval)의 OHLCV 데이터를 생성하고 저장
     * <p>Redis ZSET에 저장된 시세 데이터를 기준으로 처리</p>
     *
     * @param stock 종목 엔티티
     * @param interval 분봉 간격 (단위: 분)
     */
    private void generateMinuteCandle(Stock stock, int interval) {
        String ticker = stock.getStockTicker();

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        int currentMinute = now.getMinute();
        int normalizedMinute = (currentMinute / interval) * interval;

        LocalDateTime candleTime = now.withMinute(normalizedMinute).withSecond(0).withNano(0);
        LocalDateTime from = candleTime.minusSeconds(1);
        LocalDateTime to = candleTime.plusMinutes(interval);

        List<StockPriceDto> prices = redisService.findBetween(ticker, from, to);
        if (prices.isEmpty()) {
            log.warn("[분봉 생성] Redis 시세 없음 - {} {}분 {}", ticker, interval, from);
            return;
        }

        prices.sort(Comparator.comparing(StockPriceDto::datetime));

        long open = prices.get(0).currentPrice();
        long close = prices.get(prices.size() - 1).currentPrice();
        long high = prices.stream().mapToLong(StockPriceDto::currentPrice).max().orElse(open);
        long low = prices.stream().mapToLong(StockPriceDto::currentPrice).min().orElse(open);

        Long startVol = prices.get(0).accumulatedVolume();
        Long endVol = prices.get(prices.size() - 1).accumulatedVolume();
        if (startVol == null || endVol == null || endVol < startVol) {
            log.warn("[분봉 생성] 잘못된 누적 거래량: start={}, end={}", startVol, endVol);
            return;
        }
        long volume = endVol - startVol;

        boolean exists = minuteRepo.existsByStockAndIntervalAndCandleTime(stock, interval, from);
        if (exists) {
            log.debug("[분봉 생성] 중복 분봉 데이터 - 저장 생략: {} {}분 {}", ticker, interval, from);
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
            log.info("[분봉 생성] 저장 성공: {} {}분 {}", ticker, interval, from);
        } catch (Exception e) {
            log.error("[분봉 생성] 저장 실패: {} {}분 {}, 이유: {}", ticker, interval, from, e.getMessage(), e);
        }
    }

    /**
     * Redis ZSET에 저장된 데이터 중 시간이 오래된 데이터를 삭제
     */
    public void removeOldRedisPricesAll() {
        List<Stock> stocks = stockRepository.findAll();

        for (Stock stock : stocks) {
            redisService.removeOldPrices(stock.getStockTicker());
        }
    }
}
