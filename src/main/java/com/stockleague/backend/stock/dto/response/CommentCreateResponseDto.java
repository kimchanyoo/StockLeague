package com.stockleague.backend.stock.dto.response;

import com.stockleague.backend.stock.domain.Comment;
import java.time.format.DateTimeFormatter;

public record CommentCreateResponseDto(
        boolean success,
        String message,
        Long commentId,
        String createdAt,
        String nickname
) {
    public static CommentCreateResponseDto from(Comment comment) {
        return new CommentCreateResponseDto(
                true,
                "댓글이 등록되었습니다.",
                comment.getId(),
                comment.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                comment.getUser().getNickname()
        );
    }
}
