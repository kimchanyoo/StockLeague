package com.stockleague.backend.stock.dto.response.report;

import com.stockleague.backend.stock.domain.CommentReport;
import com.stockleague.backend.stock.domain.Status;
import java.time.format.DateTimeFormatter;

public record CommentReportSummaryDto(
        Long report_id,
        String reason,
        Status status,
        String createdAt

) {
    public static CommentReportSummaryDto from(CommentReport report) {
        return new CommentReportSummaryDto(
                report.getId(),
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
