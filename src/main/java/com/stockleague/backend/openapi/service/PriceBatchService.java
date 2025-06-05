package com.stockleague.backend.openapi.service;

import com.stockleague.backend.openapi.client.KisApiClient;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockDailyPrice;
import com.stockleague.backend.stock.domain.StockMonthlyPrice;
import com.stockleague.backend.stock.domain.StockYearlyPrice;
import com.stockleague.backend.stock.dto.response.stock.StockDailyPriceDto;
import com.stockleague.backend.stock.dto.response.stock.StockMonthlyPriceDto;
import com.stockleague.backend.stock.dto.response.stock.StockYearlyPriceDto;
import com.stockleague.backend.stock.repository.StockDailyPriceRepository;
import com.stockleague.backend.stock.repository.StockMonthlyPriceRepository;
import com.stockleague.backend.stock.repository.StockRepository;
import com.stockleague.backend.stock.repository.StockYearlyPriceRepository;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceBatchService {

    private final StockRepository stockRepository;
    private final StockYearlyPriceRepository yearlyPriceRepository;
    private final StockMonthlyPriceRepository monthlyPriceRepository;
    private final StockDailyPriceRepository dailyPriceRepository;
    private final KisApiClient kisApiClient;

    public void saveYearlyPricesByTicker(String ticker) {
        Stock stock = stockRepository.findByStockTicker(ticker)
                .orElseThrow(() -> new IllegalArgumentException("해당 티커를 가진 종목이 존재하지 않습니다: " + ticker));

        int listYear = stock.getListDate().getYear();
        int currentYear = LocalDate.now().getYear();

        for (int year = listYear; year <= currentYear; year++) {
            try {
                List<StockYearlyPriceDto> yearlyPrices = kisApiClient.getYearlyPrices(ticker, year);

                for (StockYearlyPriceDto dto : yearlyPrices) {
                    if (dto.year() == year) {
                        StockYearlyPrice entity = StockYearlyPrice.builder()
                                .stock(stock)
                                .year(dto.year())
                                .openPrice(dto.openPrice())
                                .highPrice(dto.highPrice())
                                .lowPrice(dto.lowPrice())
                                .closePrice(dto.closePrice())
                                .volume(dto.volume())
                                .build();

                        yearlyPriceRepository.save(entity);
                    }
                }

                log.info("[{}] {}년 데이터 저장 완료", ticker, year);

                Thread.sleep(1000L);

            } catch (Exception e) {
                log.warn("{}년 {} 연봉 데이터 저장 실패: {}", year, ticker, e.getMessage());
            }
        }
    }

    public void saveMonthlyPricesByTicker(String ticker) {
        Stock stock = stockRepository.findByStockTicker(ticker)
                .orElseThrow(() -> new IllegalArgumentException("해당 티커를 가진 종목이 존재하지 않습니다: " + ticker));

        LocalDate startDate = stock.getListDate().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now().withDayOfMonth(1);

        while (!startDate.isAfter(endDate)) {
            LocalDate chunkEndDate = startDate.plusMonths(99);
            if (chunkEndDate.isAfter(endDate)) chunkEndDate = endDate;

            String fromDate = startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String toDate = chunkEndDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            try {
                List<StockMonthlyPriceDto> dtos = kisApiClient.getMonthlyPrices(ticker, fromDate, toDate);
                int savedCount = 0;

                for (StockMonthlyPriceDto dto : dtos) {
                    if (monthlyPriceRepository.existsByStockAndYearAndMonth(stock, dto.year(), dto.month())) {
                        log.debug("이미 존재: {}년 {}월 - 건너뜀", dto.year(), dto.month());
                        continue;
                    }

                    StockMonthlyPrice entity = StockMonthlyPrice.builder()
                            .stock(stock)
                            .year(dto.year())
                            .month(dto.month())
                            .openPrice(dto.openPrice())
                            .highPrice(dto.highPrice())
                            .lowPrice(dto.lowPrice())
                            .closePrice(dto.closePrice())
                            .volume(dto.volume())
                            .build();

                    monthlyPriceRepository.save(entity);
                    savedCount++;
                }

                log.info("[{}] {} ~ {} 월봉 데이터 저장 완료 ({}건)", ticker, fromDate, toDate, savedCount);
                Thread.sleep(1000L);

            } catch (Exception e) {
                log.warn("[{}] {} ~ {} 월봉 데이터 저장 실패: {}", ticker, fromDate, toDate, e.getMessage());
            }

            startDate = startDate.plusMonths(100);
        }
    }

    public void saveDailyPricesByTicker(String ticker) {
        Stock stock = stockRepository.findByStockTicker(ticker)
                .orElseThrow(() -> new IllegalArgumentException("해당 티커를 가진 종목이 존재하지 않습니다: " + ticker));

        LocalDate startDate = stock.getListDate();
        LocalDate endDate = LocalDate.now();

        while (!startDate.isAfter(endDate)) {
            LocalDate chunkEndDate = startDate.plusDays(99);
            if (chunkEndDate.isAfter(endDate)) chunkEndDate = endDate;

            String fromDate = startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String toDate = chunkEndDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            try {
                List<StockDailyPriceDto> dtos = kisApiClient.getDailyPrices(ticker, fromDate, toDate);
                int savedCount = 0;

                for (StockDailyPriceDto dto : dtos) {
                    if (dailyPriceRepository.existsByStockAndDate(stock, dto.date())) {
                        log.debug("이미 존재: {} - 건너뜀", dto.date());
                        continue;
                    }

                    StockDailyPrice entity = StockDailyPrice.builder()
                            .stock(stock)
                            .date(dto.date())
                            .openPrice(dto.openPrice())
                            .highPrice(dto.highPrice())
                            .lowPrice(dto.lowPrice())
                            .closePrice(dto.closePrice())
                            .volume(dto.volume())
                            .build();

                    dailyPriceRepository.save(entity);
                    savedCount++;
                }

                log.info("[{}] {} ~ {} 일봉 데이터 저장 완료 ({}건)", ticker, fromDate, toDate, savedCount);
                Thread.sleep(1000L);

            } catch (Exception e) {
                log.warn("[{}] {} ~ {} 일봉 데이터 저장 실패: {}", ticker, fromDate, toDate, e.getMessage());
            }

            startDate = startDate.plusDays(100);
        }
    }

}