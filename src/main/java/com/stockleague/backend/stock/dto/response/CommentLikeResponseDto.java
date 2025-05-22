package com.stockleague.backend.stock.dto.response;

public record CommentLikeResponseDto(
        boolean success,
        String message,
        boolean isLiked,
        int likeCount
) {
}
