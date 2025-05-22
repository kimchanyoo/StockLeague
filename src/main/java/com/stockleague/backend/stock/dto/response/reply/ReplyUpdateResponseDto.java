package com.stockleague.backend.stock.dto.response.reply;

public record ReplyUpdateResponseDto(
        boolean success,
        String message
) {
    public static ReplyUpdateResponseDto from() {
        return new ReplyUpdateResponseDto(
                true,
                "대댓글이 성공적으로 수정되었습니다."
        );
    }
}
