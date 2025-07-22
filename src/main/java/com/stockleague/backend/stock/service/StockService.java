package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.infra.redis.StockPriceRedisService;
import com.stockleague.backend.stock.domain.Stock;
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
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private final StockRepository stockRepository;
    private final StockYearlyPriceRepository yearlyRepo;
    private final StockMonthlyPriceRepository monthlyRepo;
    private final StockWeeklyPriceRepository weeklyRepo;
    private final StockDailyPriceRepository dailyRepo;
    private final StockMinutePriceRepository minuteRepo;

    private final StockPriceRedisService stockPriceRedisService;

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
     * Redis에 저장된 특정 종목의 최신 시세 정보를 조회하고,
     * 현재 장이 열려 있는지 여부를 함께 포함한 DTO를 반환합니다.
     *
     * <p>이 메서드는 프론트엔드가 첫 접속 시 사용할 수 있는
     * "최신 시세 + 장 상태" 정보를 제공합니다.</p>
     *
     * @param ticker 종목 코드 (예: "005930")
     * @return 최신 {@link StockPriceDto} 객체 (isMarketOpen 필드 포함)
     * @throws GlobalException {@code STOCK_PRICE_NOT_FOUND} - 종목의 시세 정보가 존재하지 않는 경우
     */
    public StockPriceDto getEffectivePrice(String ticker) {
        StockPriceDto dto = stockPriceRedisService.getLatest(ticker);

        if (dto == null) {
            throw new GlobalException(GlobalErrorCode.STOCK_PRICE_NOT_FOUND);
        }

        return StockPriceDto.from(dto, isMarketTime());
    }

    /**
     * 현재 시간이 주식 시장의 개장 시간(평일 09:00~15:30)인지 여부를 반환합니다.
     *
     * <p>토요일, 일요일은 자동으로 장 외 시간으로 간주하며,
     * 공휴일은 별도 로직이 없으므로 포함되지 않습니다.</p>
     *
     * @return {@code true} - 장이 열려 있는 시간대일 경우
     *         {@code false} - 장이 닫힌 시간대일 경우
     */
    private boolean isMarketTime() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek day = now.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) return false;

        LocalTime time = now.toLocalTime();
        return !time.isBefore(LocalTime.of(9, 0)) && !time.isAfter(LocalTime.of(15, 30));
    }
}
