package com.stockleague.backend.stock.dto.response;

public record CommentDeleteResponseDto(
        boolean success,
        String message
) {
    public static CommentDeleteResponseDto from() {
        return new CommentDeleteResponseDto(
                true,
                "댓글이 삭제되었습니다.");
    }
}
