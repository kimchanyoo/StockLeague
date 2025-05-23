package com.stockleague.backend.stock.dto.response.report;

import com.stockleague.backend.stock.domain.ActionTaken;
import com.stockleague.backend.stock.domain.CommentReport;
import com.stockleague.backend.stock.domain.Status;
import java.time.format.DateTimeFormatter;

public record CommentReportDetailResponseDto(
        boolean success,
        String message,
        Long reportId,
        Long targetId,
        String reporterNickname,
        String processedByNickname,
        String reason,
        String additionalInfo,
        Status status,
        String createdAt,
        ActionTaken actionTaken
) {
    public static CommentReportDetailResponseDto from(CommentReport report) {
        return new CommentReportDetailResponseDto(
               true,
               "신고 내용 불러오기에 성공했습니다.",
                report.getId(),
                report.getTarget().getId(),
                report.getReporter().getNickname(),
                report.getProcessedBy().getNickname(),
                report.getReason(),
                report.getAdditionalInfo(),
                report.getStatus(),
                report.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                report.getActionTaken()
        );
    }
}
