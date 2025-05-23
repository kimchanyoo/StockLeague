package com.stockleague.backend.stock.dto.response.report;

import com.stockleague.backend.stock.domain.CommentReport;

public record CommentReportSummaryDto(
        Long commentId,
        String reporterNickname,
        Integer reportCount,
        Integer warningCount

) {
    public static CommentReportSummaryDto from(CommentReport report) {
        return new CommentReportSummaryDto(
                report.getComment().getId(),
                report.getReporter().getNickname(),
                report.getComment().getReportCount(),
                report.getComment().getUser().getWarningCount()
        );
    }
}
