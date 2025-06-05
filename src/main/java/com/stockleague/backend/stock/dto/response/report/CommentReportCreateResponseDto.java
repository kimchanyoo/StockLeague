package com.stockleague.backend.stock.dto.response.report;

public record CommentReportCreateResponseDto(
        boolean success,
        String message
) {
    public static CommentReportCreateResponseDto from() {
        return new CommentReportCreateResponseDto(
                true,
                "신고가 정상적으로 접수되었습니다.");
    }
}
