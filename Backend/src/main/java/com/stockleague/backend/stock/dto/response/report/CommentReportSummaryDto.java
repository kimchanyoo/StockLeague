package com.stockleague.backend.stock.dto.response.report;

import com.stockleague.backend.stock.domain.Comment;
import com.stockleague.backend.stock.domain.CommentReport;
import com.stockleague.backend.stock.domain.Status;

public record CommentReportSummaryDto(
        Long commentId,
        String authorNickname,
        Integer reportCount,
        Integer warningCount,
        Status status

) {
    public static CommentReportSummaryDto from(CommentReport report) {
        Comment comment = report.getComment();

        return new CommentReportSummaryDto(
                comment.getId(),
                comment.getUser().getNickname(),
                comment.getReportCount(),
                comment.getUser().getWarningCount(),
                comment.getStatus()
        );
    }
}
