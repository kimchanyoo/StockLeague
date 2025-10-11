package com.stockleague.backend.stock.dto.request.report;


import com.stockleague.backend.stock.domain.Reason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentReportCreateRequestDto(

        @NotNull(message = "신고 사유는 비어 있을 수 없습니다.")
        Reason reason,

        @NotBlank(message = "신고 상세 내용은 비어 있을 수 없습니다.")
        String additionalInfo
) {
}
