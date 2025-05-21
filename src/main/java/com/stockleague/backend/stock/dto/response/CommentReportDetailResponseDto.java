package com.stockleague.backend.stock.dto.response;

import com.stockleague.backend.stock.domain.ActionTaken;
import com.stockleague.backend.stock.domain.CommentReport;
import com.stockleague.backend.stock.domain.Status;
import com.stockleague.backend.stock.domain.TargetType;
import java.time.format.DateTimeFormatter;

public record CommentReportDetailResponseDto(
        boolean success,
        String message,
        Long reportId,
        TargetType targetType,
        Long targetId,
        String reporterNickname,
        String processedByNickname,
        String reason,
        String additionalInfo,
        Status status,
        String createdAt,
        String processedAt,
        ActionTaken actionTaken
) {
    public static CommentReportDetailResponseDto from(CommentReport report) {
        return new CommentReportDetailResponseDto(
               true,
               "신고 내용 불러오기에 성공했습니다.",
                report.getId(),
                report.getTargetType(),
                report.getTarget().getId(),
                report.getReporter().getNickname(),
                report.getProcessedBy().getNickname(),
                report.getReason(),
                report.getAdditionalInfo(),
                report.getStatus(),
                report.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                report.getProcessedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                report.getActionTaken()
        );
    }
}
