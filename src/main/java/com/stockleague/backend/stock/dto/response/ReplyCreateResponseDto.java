package com.stockleague.backend.stock.dto.response;

import com.stockleague.backend.stock.domain.Comment;
import java.time.format.DateTimeFormatter;

public record ReplyCreateResponseDto(
        boolean success,
        String message,
        Long replyId,
        String createdAt,
        String nickname
) {
    public static ReplyCreateResponseDto from(Comment comment) {
        return new ReplyCreateResponseDto(
                true,
                "답글이 등록되었습니다.",
                comment.getId(),
                comment.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                comment.getUser().getNickname()
        );
    }
}
