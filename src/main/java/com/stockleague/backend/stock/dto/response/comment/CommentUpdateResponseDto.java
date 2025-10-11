package com.stockleague.backend.stock.dto.response.comment;

public record CommentUpdateResponseDto(
        boolean success,
        String message
) {
    public static CommentUpdateResponseDto from() {
        return new CommentUpdateResponseDto(
                true,
                "댓글이 성공적으로 수정되었습니다."
        );
    }
}
