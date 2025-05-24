package com.stockleague.backend.stock.dto.response.report;

import java.util.List;

public record CommentReportDetailResponseDto(
        boolean success,
        String message,
        Long commentId,
        String commentAuthorNickname,
        String commentCreatedAt,
        String stockName,
        String commentContent,
        Long commentAuthorId,
        int warningCount,
        Boolean accountStatus,
        List<ReportDetailDto> reports,
        List<WarningHistoryDto> warnings
) {
}
