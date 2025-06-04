package com.stockleague.backend.openapi.service;

import com.stockleague.backend.openapi.client.KisApiClient;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockMonthlyPrice;
import com.stockleague.backend.stock.domain.StockYearlyPrice;
import com.stockleague.backend.stock.dto.response.stock.StockMonthlyPriceDto;
import com.stockleague.backend.stock.dto.response.stock.StockYearlyPriceDto;
import com.stockleague.backend.stock.repository.StockMonthlyPriceRepository;
import com.stockleague.backend.stock.repository.StockRepository;
import com.stockleague.backend.stock.repository.StockYearlyPriceRepository;
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
            int year = startDate.getYear();
            int month = startDate.getMonthValue();

            try {
                List<StockMonthlyPriceDto> monthlyPrices = kisApiClient.getMonthlyPrices(ticker, year, month);

                for (StockMonthlyPriceDto dto : monthlyPrices) {
                    if (dto.year() == year && dto.month() == month) {
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
                    }
                }

                log.info("[{}] {}년 {}월 데이터 저장 완료", ticker, year, month);
                Thread.sleep(1000L);

            } catch (Exception e) {
                log.warn("{}년 {}월 {} 월봉 데이터 저장 실패: {}", year, month, ticker, e.getMessage());
            }

            startDate = startDate.plusMonths(1);
        }
    }

}