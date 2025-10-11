package com.stockleague.backend.stock.dto.response.comment;

public record CommentAdminDeleteResponseDto(
        boolean success,
        String message
) {
    public static CommentAdminDeleteResponseDto from() {
        return new CommentAdminDeleteResponseDto(
                true,
                "댓글이 삭제 처리되었습니다."
        );
    }
}
