package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.stock.domain.Comment;
import com.stockleague.backend.stock.domain.CommentLike;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.dto.request.CommentCreateRequestDto;
import com.stockleague.backend.stock.dto.request.CommentUpdateRequestDto;
import com.stockleague.backend.stock.dto.response.CommentCreateResponseDto;
import com.stockleague.backend.stock.dto.response.CommentLikeResponseDto;
import com.stockleague.backend.stock.dto.response.CommentUpdateResponseDto;
import com.stockleague.backend.stock.repository.CommentLikeRepository;
import com.stockleague.backend.stock.repository.CommentRepository;
import com.stockleague.backend.stock.repository.StockRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.repository.UserRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

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

    @Transactional
    public CommentLikeResponseDto toggleCommentLike(Long commentId, Long userId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.COMMENT_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        CommentLike like = commentLikeRepository.findByUserIdAndCommentId(userId, commentId)
                .orElse(null);

        if (like == null) {
            commentLikeRepository.save(CommentLike.builder()
                    .user(user)
                    .comment(comment)
                    .build());
            return new CommentLikeResponseDto(true, "좋아요가 등록되었습니다.",
                    true, comment.getLikeCount());
        } else {
            like.toggle();
            if (like.getIsLiked()) {
                comment.increaseLikeCount();
                return new CommentLikeResponseDto(true, "좋아요가 등록되었습니다.",
                        like.getIsLiked(), comment.getLikeCount());
            } else {
                comment.decreaseLikeCount();
                return new CommentLikeResponseDto(true, "좋아요가 취소되었습니다.",
                        like.getIsLiked(), comment.getLikeCount());
            }
        }
    }

    @Transactional
    public CommentUpdateResponseDto updateComment(CommentUpdateRequestDto request,
                                                  Long commentId, Long userId) {

        if (request.content().isBlank()) {
            throw new GlobalException(GlobalErrorCode.MISSING_FIELDS);
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.COMMENT_NOT_FOUND));

        if(!Objects.equals(comment.getUser().getId(), userId)) {
            throw new GlobalException(GlobalErrorCode.INVALID_COMMENT_OWNER);
        }

        comment.updateContent(request.content());

        return CommentUpdateResponseDto.from();
    }
}
