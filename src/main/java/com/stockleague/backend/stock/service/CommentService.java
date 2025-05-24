package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.notification.domain.NotificationType;
import com.stockleague.backend.notification.domain.TargetType;
import com.stockleague.backend.notification.dto.NotificationEvent;
import com.stockleague.backend.notification.kafka.producer.NotificationProducer;
import com.stockleague.backend.stock.domain.Comment;
import com.stockleague.backend.stock.domain.CommentLike;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.dto.request.comment.CommentCreateRequestDto;
import com.stockleague.backend.stock.dto.request.comment.CommentUpdateRequestDto;
import com.stockleague.backend.stock.dto.response.comment.CommentAdminDeleteResponseDto;
import com.stockleague.backend.stock.dto.response.comment.CommentCreateResponseDto;
import com.stockleague.backend.stock.dto.response.comment.CommentDeleteResponseDto;
import com.stockleague.backend.stock.dto.response.comment.CommentLikeResponseDto;
import com.stockleague.backend.stock.dto.response.comment.CommentListResponseDto;
import com.stockleague.backend.stock.dto.response.comment.CommentSummaryDto;
import com.stockleague.backend.stock.dto.response.comment.CommentUpdateResponseDto;
import com.stockleague.backend.stock.repository.CommentLikeRepository;
import com.stockleague.backend.stock.repository.CommentRepository;
import com.stockleague.backend.stock.repository.StockRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final NotificationProducer notificationProducer;

    public CommentCreateResponseDto createComment(
            CommentCreateRequestDto request, String ticker, Long userId) {

        if (request.content().isBlank()) {
            throw new GlobalException(GlobalErrorCode.MISSING_FIELDS);
        }

        Stock stock = stockRepository.findByStockTicker(ticker)
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

    @Transactional
    public CommentDeleteResponseDto deleteComment(Long commentId, Long userId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.COMMENT_NOT_FOUND));

        if(!Objects.equals(comment.getUser().getId(), userId)) {
            throw new GlobalException(GlobalErrorCode.INVALID_COMMENT_OWNER);
        }

        comment.markAsDeleted();

        return CommentDeleteResponseDto.from();
    }

    public CommentListResponseDto getComments(String ticker, Long userId, int page, int size) {

        if (page < 1 || size < 1) {
            throw new GlobalException(GlobalErrorCode.INVALID_PAGINATION);
        }

        Stock stock = stockRepository.findByStockTicker(ticker)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.STOCK_NOT_FOUND));

        PageRequest pageable = PageRequest.of(
                page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Comment> commentPage = commentRepository.findByStockIdAndParentIsNullAndDeletedAtIsNull(stock.getId(), pageable);

        List<Comment> comments = commentPage.getContent();

        List<Long> commentIds = comments.stream().map(Comment::getId).toList();
        List<Long> likedIds = userId != null && !commentIds.isEmpty()
                ? commentLikeRepository.findLikedCommentIdsByUserIdAndCommentIds(userId, commentIds)
                : List.of();

        Set<Long> likedSet = new HashSet<>(likedIds);

        List<CommentSummaryDto> commentList = comments.stream()
                .map(comment -> CommentSummaryDto.from(comment, userId, likedSet.contains(comment.getId())))
                .toList();

        return new CommentListResponseDto(true, commentList, page, size, commentPage.getTotalElements());
    }

    @Transactional
    public CommentAdminDeleteResponseDto forceDeleteCommentByAdmin(Long commentId, Long adminId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.COMMENT_NOT_FOUND));

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        comment.markDeletedByAdmin(admin);

        NotificationEvent event = new NotificationEvent(
                comment.getUser().getId(),
                NotificationType.COMMENT_DELETED,
                TargetType.COMMENT,
                commentId
        );
        notificationProducer.send(event);

        return CommentAdminDeleteResponseDto.from();
    }
}
