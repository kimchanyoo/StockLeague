package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.dto.response.stock.CandleDto;
import com.stockleague.backend.stock.dto.response.stock.StockListResponseDto;
import com.stockleague.backend.stock.dto.response.stock.StockSummaryDto;
import com.stockleague.backend.stock.repository.StockDailyPriceRepository;
import com.stockleague.backend.stock.repository.StockMonthlyPriceRepository;
import com.stockleague.backend.stock.repository.StockRepository;
import com.stockleague.backend.stock.repository.StockYearlyPriceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockYearlyPriceRepository yearlyRepo;
    private final StockMonthlyPriceRepository monthlyRepo;
    private final StockDailyPriceRepository dailyRepo;

    public StockListResponseDto getAllStocks() {

        Pageable topTen = PageRequest.of(0, 10);

        List<Stock> stocks = stockRepository.findAll(topTen).getContent();

        List<StockSummaryDto> stockDtos = stocks.stream()
                .map(StockSummaryDto::from)
                .toList();

        return new StockListResponseDto(true, "종목 리스트 조회 테스트", stockDtos);
    }

    public List<CandleDto> getCandles(String ticker, String interval, int offset, int limit) {

        Stock stock = stockRepository.findByStockTicker(ticker)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.STOCK_NOT_FOUND));

        Long stockId = stock.getId();
        Pageable pageable = PageRequest.of(offset / limit, limit);

        return switch (interval) {
            case "y" -> yearlyRepo.findAllByStockIdOrderByYearDesc(stockId, pageable)
                    .map(CandleDto::from)
                    .toList();
            case "M" -> monthlyRepo.findAllByStockIdOrderByMonthDesc(stockId, pageable)
                    .map(CandleDto::from)
                    .toList();
            case "d" -> dailyRepo.findAllByStockIdOrderByDateDesc(stockId, pageable)
                    .map(CandleDto::from)
                    .toList();
            default -> throw new IllegalArgumentException("지원하지 않는 interval입니다: " + interval);
        };
    }
}
