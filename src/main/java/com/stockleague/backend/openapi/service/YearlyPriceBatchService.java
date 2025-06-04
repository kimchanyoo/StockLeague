package com.stockleague.backend.openapi.service;

import com.stockleague.backend.openapi.client.KisApiClient;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockYearlyPrice;
import com.stockleague.backend.stock.dto.response.stock.StockYearlyPriceDto;
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
public class YearlyPriceBatchService {

    private final StockRepository stockRepository;
    private final StockYearlyPriceRepository yearlyPriceRepository;
    private final KisApiClient kisApiClient;

    public void saveYearlyPricesByTicker(String ticker) {
        Stock stock = stockRepository.findByStockTicker(ticker)
                .orElseThrow(() -> new IllegalArgumentException("해당 티커를 가진 종목이 존재하지 않습니다: " + ticker));

        int listYear = stock.getListDate().getYear();
        int currentYear = LocalDate.now().getYear();

        for (int year = listYear; year <= currentYear; year++) {
            int retry = 0;
            boolean success = false;

            while (retry < 3 && !success) {
                try {
                    // 요청 텀을 두기 위한 슬립 (200ms 이상 권장)
                    Thread.sleep(300);

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
                    success = true;

                } catch (Exception e) {
                    retry++;
                    log.warn("[{}] {}년 데이터 저장 실패 ({}회 시도): {}", ticker, year, retry, e.getMessage());

                    try {
                        Thread.sleep(1000L * retry); // 1초, 2초, 3초
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }

                    if (retry == 3) {
                        log.error("[{}] {}년 데이터 저장 실패 - 최종 포기", ticker, year);
                    }
                }
            }
        }
    }
}