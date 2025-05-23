package com.stockleague.backend.stock.dto.response.report;

import com.stockleague.backend.stock.domain.CommentReport;
import com.stockleague.backend.stock.domain.Reason;
import java.time.format.DateTimeFormatter;

public record ReportDetailDto(
        String reporterNickname,
        Reason reason,
        String additionalInfo,
        String reportedAt
) {
    public static ReportDetailDto from(CommentReport report) {
        return new ReportDetailDto(
                report.getReporter().getNickname(),
                report.getReason(),
                report.getAdditionalInfo(),
                report.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
