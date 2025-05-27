package com.stockleague.backend.stock.dto.response.report;

import com.stockleague.backend.stock.domain.ActionTaken;
import com.stockleague.backend.stock.domain.Status;
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
        String AdminNickname,
        ActionTaken actionTaken,
        Status status,
        List<ReportDetailDto> reports,
        List<WarningHistoryDto> warnings
) {
}
