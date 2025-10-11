package com.stockleague.backend.stock.dto.response.reply;

import com.stockleague.backend.stock.domain.Comment;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

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
    public static ReplySummaryDto from(Comment reply, Long userId, boolean isLiked) {
        return new ReplySummaryDto(
                reply.getId(),
                reply.getParent().getId(),
                reply.getUser().getNickname(),
                reply.getContent(),
                reply.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                Objects.equals(reply.getUser().getId(), userId),
                reply.getLikeCount(),
                isLiked
        );
    }
}
