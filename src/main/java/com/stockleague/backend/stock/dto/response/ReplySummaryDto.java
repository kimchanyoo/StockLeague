package com.stockleague.backend.stock.dto.response;

import com.stockleague.backend.stock.domain.Comment;
import java.time.format.DateTimeFormatter;

public record ReplySummaryDto(
        Long replyId,
        Long commentId,
        String userNickname,
        String content,
        String createdAt,
        boolean isAuthor,
        int likeCount,
        boolean isLiked
) {
    public static ReplySummaryDto from(Comment reply, Long currentUserId, boolean isLiked) {
        return new ReplySummaryDto(
                reply.getId(),
                reply.getParent().getId(),
                reply.getUser().getNickname(),
                reply.getContent(),
                reply.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                reply.getUser().getId().equals(currentUserId),
                reply.getLikeCount(),
                isLiked
        );
    }
}
