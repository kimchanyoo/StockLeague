package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.stock.domain.Comment;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.dto.request.CommentCreateRequestDto;
import com.stockleague.backend.stock.dto.response.CommentCreateResponseDto;
import com.stockleague.backend.stock.repository.CommentRepository;
import com.stockleague.backend.stock.repository.StockRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final CommentRepository commentRepository;

    public CommentCreateResponseDto createComment(
            CommentCreateRequestDto request, String ticker, Long userId) {

        if (request.content().isBlank()) {
            throw new GlobalException(GlobalErrorCode.MISSING_FIELDS);
        }

        Stock stock = stockRepository.findByTicker(ticker)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.STOCK_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        Comment comment = Comment.builder()
                .stock(stock)
                .user(user)
                .content(request.content())
                .build();

        commentRepository.save(comment);

        return CommentCreateResponseDto.from(comment);
    }
}
