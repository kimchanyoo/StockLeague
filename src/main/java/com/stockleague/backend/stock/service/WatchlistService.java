package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.Watchlist;
import com.stockleague.backend.stock.dto.request.watchlist.WatchlistCreateRequestDto;
import com.stockleague.backend.stock.dto.response.watchlist.WatchlistCreateResponseDto;
import com.stockleague.backend.stock.dto.response.watchlist.WatchlistListResponseDto;
import com.stockleague.backend.stock.dto.response.watchlist.WatchlistSummaryDto;
import com.stockleague.backend.stock.repository.StockRepository;
import com.stockleague.backend.stock.repository.WatchlistRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    public WatchlistListResponseDto getWatchlist(Long userId, int page, int size){

        if (page < 1 || size < 1) {
            throw new GlobalException(GlobalErrorCode.INVALID_PAGINATION);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        PageRequest pageable = PageRequest.of(
                page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Watchlist> watchlistPage = watchlistRepository.findAllByUser(user, pageable);

        List<WatchlistSummaryDto> content = watchlistPage.getContent()
                .stream()
                .map(WatchlistSummaryDto::from)
                .collect(Collectors.toList());

        return new WatchlistListResponseDto(
                true,
                content,
                page,
                size,
                watchlistPage.getTotalElements());
    }
}
