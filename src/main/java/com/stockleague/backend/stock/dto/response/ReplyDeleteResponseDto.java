package com.stockleague.backend.stock.dto.response;

public record ReplyDeleteResponseDto(
        boolean success,
        String message
) {
    public static ReplyDeleteResponseDto from() {
        return new ReplyDeleteResponseDto(
                true,
                "대댓글이 삭제되었습니다."
        );
    }
}
