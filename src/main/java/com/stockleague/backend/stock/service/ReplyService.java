package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.stock.domain.Comment;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.dto.request.reply.ReplyCreateRequestDto;
import com.stockleague.backend.stock.dto.request.reply.ReplyUpdateRequestDto;
import com.stockleague.backend.stock.dto.response.reply.ReplyCreateResponseDto;
import com.stockleague.backend.stock.dto.response.reply.ReplyDeleteResponseDto;
import com.stockleague.backend.stock.dto.response.reply.ReplyListResponseDto;
import com.stockleague.backend.stock.dto.response.reply.ReplySummaryDto;
import com.stockleague.backend.stock.dto.response.reply.ReplyUpdateResponseDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    public ReplyCreateResponseDto createReply(ReplyCreateRequestDto request,
                                              String ticker, Long commentId, Long userId) {

        if (request.content().isBlank()) {
            throw new GlobalException(GlobalErrorCode.MISSING_FIELDS);
        }

        Stock stock = stockRepository.findByStockTicker(ticker)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.STOCK_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        Comment parent = commentRepository.findById(commentId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.COMMENT_NOT_FOUND));

        Comment reply = Comment.builder()
                .stock(stock)
                .user(user)
                .content(request.content())
                .build();

        parent.addReply(reply);

        commentRepository.save(reply);

        return ReplyCreateResponseDto.from(reply);
    }

    @Transactional
    public ReplyUpdateResponseDto updateReply(ReplyUpdateRequestDto request,
                                              Long replyId, Long userId) {

        if (request.content().isBlank()) {
            throw new GlobalException(GlobalErrorCode.MISSING_FIELDS);
        }

        Comment reply = commentRepository.findById(replyId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.COMMENT_NOT_FOUND));

        if(!Objects.equals(reply.getUser().getId(), userId)) {
            throw new GlobalException(GlobalErrorCode.INVALID_COMMENT_OWNER);
        }

        reply.updateContent(request.content());

        return ReplyUpdateResponseDto.from();
    }

    @Transactional
    public ReplyDeleteResponseDto deleteReply(Long replyId, Long userId) {

        Comment reply = commentRepository.findById(replyId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.COMMENT_NOT_FOUND));

        if(!Objects.equals(reply.getUser().getId(), userId)) {
            throw new GlobalException(GlobalErrorCode.INVALID_COMMENT_OWNER);
        }

        Comment parent = reply.getParent();
        if (parent != null) {
            parent.removeReply(reply);
        }

        commentRepository.delete(reply);

        return ReplyDeleteResponseDto.from();
    }

    public ReplyListResponseDto getReplies(Long commentId, Long userId) {

        commentRepository.findById(commentId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.COMMENT_NOT_FOUND));

        List<Comment> replies = commentRepository.findByParentId(commentId);

        List<Long> replyIds = replies.stream().map(Comment::getId).toList();

        List<Long> likedIds = replyIds.isEmpty() ? List.of() :
                commentLikeRepository.findLikedCommentIdsByUserIdAndCommentIds(userId, replyIds);

        Set<Long> likedSet = new HashSet<>(likedIds);

        List<ReplySummaryDto> result = replies.stream()
                .map(reply -> ReplySummaryDto.from(reply, userId, likedSet.contains(reply.getId())))
                .toList();

        return ReplyListResponseDto.from(result);
    }
}
