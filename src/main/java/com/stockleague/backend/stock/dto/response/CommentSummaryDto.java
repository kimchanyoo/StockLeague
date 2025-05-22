package com.stockleague.backend.stock.dto.response;

import com.stockleague.backend.stock.domain.Comment;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public record CommentSummaryDto(
        Long commentId,
        String userNickname,
        String content,
        String createdAt,
        boolean isAuthor,
        int likeCount,
        boolean isLiked,
        int replyCount
) {
    public static CommentSummaryDto from(Comment comment, Long userId, boolean isLiked) {
        return new CommentSummaryDto(
                comment.getId(),
                comment.getUser().getNickname(),
                comment.getContent(),
                comment.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                Objects.equals(comment.getUser().getId(), userId),
                comment.getLikeCount(),
                isLiked,
                comment.getReplyCount()
        );
    }
}
