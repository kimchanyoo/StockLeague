package com.stockleague.backend.stock.dto.request;


import jakarta.validation.constraints.NotBlank;

public record CommentReportRequestDto(

        @NotBlank(message = "신고 사유는 비어 있을 수 없습니다.")
        String reason,

        @NotBlank(message = "신고 상세 내용은 비어 있을 수 없습니다.")
        String additionalInfo
) {
}
