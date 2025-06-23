package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.infra.redis.StockPriceRedisService;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockMinutePrice;
import com.stockleague.backend.stock.dto.response.stock.CandleDto;
import com.stockleague.backend.stock.dto.response.stock.StockListResponseDto;
import com.stockleague.backend.stock.dto.response.stock.StockPriceDto;
import com.stockleague.backend.stock.dto.response.stock.StockSummaryDto;
import com.stockleague.backend.stock.repository.StockDailyPriceRepository;
import com.stockleague.backend.stock.repository.StockMinutePriceRepository;
import com.stockleague.backend.stock.repository.StockMonthlyPriceRepository;
import com.stockleague.backend.stock.repository.StockRepository;
import com.stockleague.backend.stock.repository.StockWeeklyPriceRepository;
import com.stockleague.backend.stock.repository.StockYearlyPriceRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockPriceRedisService redisService;
    private final StockRepository stockRepository;
    private final StockYearlyPriceRepository yearlyRepo;
    private final StockMonthlyPriceRepository monthlyRepo;
    private final StockWeeklyPriceRepository weeklyRepo;
    private final StockDailyPriceRepository dailyRepo;
    private final StockMinutePriceRepository minuteRepo;

    public StockListResponseDto getAllStocks() {

        Pageable topTen = PageRequest.of(0, 10);

        List<String> tickers = List.of("005930", "000660");

        List<Stock> stocks = stockRepository.findByStockTickerIn(tickers, topTen);

        List<StockSummaryDto> stockDtos = stocks.stream()
                .map(StockSummaryDto::from)
                .toList();

        return new StockListResponseDto(true, "종목 리스트 조회 테스트", stockDtos);
    }

    /**
     * 주어진 종목 티커(ticker)와 캔들 타입(interval)에 따라 캔들 데이터를 페이징 조회한다.
     *
     * <p>지원하는 interval 값:</p>
     * <ul>
     *   <li>"y" - 연봉</li>
     *   <li>"m" - 월봉</li>
     *   <li>"w" - 주봉</li>
     *   <li>"d" - 일봉</li>
     *   <li>"1", "3", "5", "10", "15", "30", "60" - 분봉 (정수 문자열)</li>
     * </ul>
     * 페이징은 offset과 limit 기반이며, 최신 순으로 정렬된 데이터를 반환한다.
     *
     * @param ticker   조회할 종목 티커 (예: "005930")
     * @param interval 캔들 타입 ("y", "m", "w", "d", 또는 분 단위 문자열: "1", "3" 등)
     * @param offset   페이징 offset (0부터 시작)
     * @param limit    페이지당 데이터 개수
     * @return CandleDto 리스트 (최신 순 정렬)
     * @throws GlobalException 종목이 존재하지 않을 경우
     * @throws IllegalArgumentException 지원하지 않는 interval인 경우
     */
    public List<CandleDto> getCandles(String ticker, String interval, int offset, int limit) {

        Stock stock = stockRepository.findByStockTicker(ticker)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.STOCK_NOT_FOUND));

        Long stockId = stock.getId();
        Pageable pageable = PageRequest.of(offset / limit, limit);

        return switch (interval) {
            case "y" -> yearlyRepo.findAllByStockIdOrderByYearDesc(stockId, pageable)
                    .map(CandleDto::from)
                    .toList();
            case "m" -> monthlyRepo.findAllByStockIdOrderByMonthDesc(stockId, pageable)
                    .map(CandleDto::from)
                    .toList();
            case "w" -> weeklyRepo.findAllByStockIdOrderByWeekDesc(stockId, pageable)
                    .map(CandleDto::from)
                    .toList();
            case "d" -> dailyRepo.findAllByStockIdOrderByDateDesc(stockId, pageable)
                    .map(CandleDto::from)
                    .toList();
            default -> {
                if (interval.matches("\\d+")) {
                    int minuteInterval = Integer.parseInt(interval);
                    yield minuteRepo.findAllByStockIdAndIntervalOrderByCandleTimeDesc(stockId, minuteInterval, pageable)
                            .map(CandleDto::from)
                            .toList();
                } else {
                    throw new IllegalArgumentException("지원하지 않는 interval입니다: " + interval);
                }
            }
        };
    }

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

            generateCandle(stock, interval);
        }
    }

    /**
     * 단일 종목에 대해 특정 분봉 간격(interval)의 OHLCV 데이터를 생성하고 저장
     * <p>Redis ZSET에 저장된 시세 데이터를 기준으로 처리</p>
     *
     * @param stock 종목 엔티티
     * @param interval 분봉 간격 (단위: 분)
     */
    private void generateCandle(Stock stock, int interval) {
        String ticker = stock.getStockTicker();

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        int currentMinute = now.getMinute();
        int normalizedMinute = (currentMinute / interval) * interval;
        LocalDateTime candleTime = now.withMinute(normalizedMinute).withSecond(0).withNano(0);

        LocalDateTime from = candleTime;
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
