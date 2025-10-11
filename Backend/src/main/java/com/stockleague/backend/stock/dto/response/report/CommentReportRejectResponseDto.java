package com.stockleague.backend.stock.dto.response.report;

public record CommentReportRejectResponseDto(
        boolean success,
        String message
) {
    public static CommentReportRejectResponseDto from() {
        return new CommentReportRejectResponseDto(
                true,
                "신고가 정상적으로 반려되었습니다.");
    }
}
