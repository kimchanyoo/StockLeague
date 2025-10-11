package com.stockleague.backend.stock.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequestDto(

        @NotBlank(message = "댓글 내용은 비어 있을 수 없습니다.")
        @Size(min = 1, max = 150, message = "댓글은 최소 1자 이상, 최대 150자까지 입력 가능합니다.")
        String content
) {
}
