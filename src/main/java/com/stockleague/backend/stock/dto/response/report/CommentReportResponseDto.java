package com.stockleague.backend.stock.dto.response.report;

public record CommentReportResponseDto(
        boolean success,
        String message
) {
    public static CommentReportResponseDto from() {
        return new CommentReportResponseDto(
                true,
                "신고가 정상적으로 접수되었습니다.");
    }
}
