package com.stockleague.backend.stock.service;

import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.dto.response.stock.StockListResponseDto;
import com.stockleague.backend.stock.dto.response.stock.StockSummaryDto;
import com.stockleague.backend.stock.repository.StockRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    public StockListResponseDto getAllStocks() {

        Pageable topTen = PageRequest.of(0, 10);

        List<Stock> stocks = stockRepository.findAll(topTen).getContent();

        List<StockSummaryDto> stockDtos = stocks.stream()
                .map(StockSummaryDto::from)
                .toList();

        return new StockListResponseDto(true, "종목 리스트 조회 테스트", stockDtos);
    }
}
