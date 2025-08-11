package com.stockleague.backend.stock.service;

import static com.stockleague.backend.global.util.MarketTimeUtil.isMarketOpen;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.infra.redis.StockPriceRedisService;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.dto.response.stock.CandleDto;
import com.stockleague.backend.stock.dto.response.stock.StockListResponseDto;
import com.stockleague.backend.stock.dto.response.stock.StockPriceDto;
import com.stockleague.backend.stock.dto.response.stock.StockSummaryDto;
import com.stockleague.backend.stock.repository.CommentRepository;
import com.stockleague.backend.stock.repository.StockDailyPriceRepository;
import com.stockleague.backend.stock.repository.StockMinutePriceRepository;
import com.stockleague.backend.stock.repository.StockMonthlyPriceRepository;
import com.stockleague.backend.stock.repository.StockRepository;
import com.stockleague.backend.stock.repository.StockWeeklyPriceRepository;
import com.stockleague.backend.stock.repository.StockYearlyPriceRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final CommentRepository commentRepository;
    private final StockRepository stockRepository;
    private final StockYearlyPriceRepository yearlyRepo;
    private final StockMonthlyPriceRepository monthlyRepo;
    private final StockWeeklyPriceRepository weeklyRepo;
    private final StockDailyPriceRepository dailyRepo;
    private final StockMinutePriceRepository minuteRepo;

    private final StockPriceRedisService stockPriceRedisService;

    /**
     * 전체 종목 목록을 페이지 단위로 조회합니다.
     * <p>
     * 종목명(stockName)을 기준으로 오름차순 정렬되며,
     * 클라이언트에서 요청한 페이지 번호(page)와 페이지 크기(size)를 기반으로 페이징 처리됩니다.
     * </p>
     *
     * @param page 조회할 페이지 번호 (1부터 시작)
     * @param size 페이지당 항목 수
     * @return 페이징된 종목 리스트와 함께 성공 여부, 메시지, 전체 항목 수를 포함한 응답 DTO
     * @throws GlobalException 페이지 번호 또는 크기가 1 미만인 경우 {@code INVALID_PAGINATION} 예외 발생
     */
    public StockListResponseDto getStocks(int page, int size) {
        if (page < 1 || size < 1) {
            throw new GlobalException(GlobalErrorCode.INVALID_PAGINATION);
        }

        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "stockName"));

        Page<Stock> stockPage = stockRepository.findAll(pageable);

        List<StockSummaryDto> stockDtos = stockPage.getContent().stream()
                .map(StockSummaryDto::from)
                .toList();

        return new StockListResponseDto(
                true,
                "종목 리스트 조회 성공",
                stockDtos,
                page,
                size,
                stockPage.getTotalElements()
        );
    }

    public StockListResponseDto getPopularStocks(int page, int size) {
        if (page < 1 || size < 1) {
            throw new GlobalException(GlobalErrorCode.INVALID_PAGINATION);
        }

        Pageable pageable = PageRequest.of(page - 1, size);

        List<Long> popularIds = commentRepository.findPopularStockIds(pageable).stream()
                .map(r -> (Long) r[0])
                .toList();

        int remainingSize = size - popularIds.size();
        List<Long> fallbackIds = remainingSize > 0
                ? stockRepository.findStockIdsWithoutComments(PageRequest.of(0, remainingSize))
                : List.of();

        List<Long> combinedIds = Stream.concat(popularIds.stream(), fallbackIds.stream())
                .limit(size)
                .toList();

        List<Stock> stocks = stockRepository.findAllById(combinedIds);
        Map<Long, Stock> stockMap = stocks.stream()
                .collect(Collectors.toMap(Stock::getId, Function.identity()));

        List<StockSummaryDto> stockDtos = combinedIds.stream()
                .map(stockMap::get)
                .filter(Objects::nonNull)
                .map(StockSummaryDto::from)
                .toList();

        return new StockListResponseDto(
                true,
                "인기 종목 조회 성공",
                stockDtos,
                page,
                size,
                stockDtos.size()
        );
    }

    /**
     * 종목명을 기준으로 키워드 검색을 수행합니다.
     * <p>
     * 종목명(`stockName`)에 입력된 키워드가 포함된 종목들을 조회하며,
     * 오름차순 정렬 및 페이징 처리된 결과를 반환합니다.
     * </p>
     *
     * @param keyword 검색할 키워드 (종목명 일부 문자열)
     * @param page 조회할 페이지 번호 (1부터 시작)
     * @param size 페이지당 항목 수
     * @return 검색 결과로 페이징된 종목 리스트와 메타 정보가 포함된 응답 DTO
     * @throws GlobalException 페이지 번호 또는 크기가 1 미만인 경우 {@code INVALID_PAGINATION} 예외 발생
     */
    public StockListResponseDto searchStocks(String keyword, int page, int size) {
        if (page < 1 || size < 1) {
            throw new GlobalException(GlobalErrorCode.INVALID_PAGINATION);
        }

        PageRequest pageable = PageRequest.of(
                page - 1, size, Sort.by(Sort.Direction.ASC, "stockName"));

        Page<Stock> stockPage = stockRepository.findByStockNameContaining(keyword, pageable);

        List<StockSummaryDto> stockDtos = stockPage.getContent().stream()
                .map(StockSummaryDto::from)
                .toList();

        return new StockListResponseDto(
                true,
                "종목 검색 결과",
                stockDtos,
                page,
                size,
                stockPage.getTotalElements()
        );
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

        return StockPriceDto.from(dto, isMarketOpen());
    }
}
