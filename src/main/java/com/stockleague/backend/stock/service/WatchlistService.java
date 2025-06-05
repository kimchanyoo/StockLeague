package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.Watchlist;
import com.stockleague.backend.stock.dto.request.watchlist.WatchlistCreateRequestDto;
import com.stockleague.backend.stock.dto.response.watchlist.WatchlistCreateResponseDto;
import com.stockleague.backend.stock.repository.StockRepository;
import com.stockleague.backend.stock.repository.WatchlistRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;


    public WatchlistCreateResponseDto createWatchlist(Long userId, WatchlistCreateRequestDto request) {

        if (request.ticker().isBlank()) {
            throw new GlobalException(GlobalErrorCode.MISSING_FIELDS);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        Stock stock = stockRepository.findByStockTicker(request.ticker())
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.STOCK_NOT_FOUND));


        Watchlist watchlist = Watchlist.builder()
                .user(user)
                .stock(stock)
                .build();

        watchlistRepository.save(watchlist);

        return WatchlistCreateResponseDto.from(watchlist);
    }
}
