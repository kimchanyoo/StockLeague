package com.stockleague.backend.stock.dto.response.comment;

public record CommentLikeResponseDto(
        boolean success,
        String message,
        boolean isLiked,
        int likeCount
) {
}
