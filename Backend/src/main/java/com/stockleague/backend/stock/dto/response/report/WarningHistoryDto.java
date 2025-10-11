package com.stockleague.backend.stock.dto.response.report;

import com.stockleague.backend.stock.domain.Reason;
import com.stockleague.backend.user.domain.UserWarning;
import java.time.format.DateTimeFormatter;

public record WarningHistoryDto(
        String warningAt,
        Reason reason,
        Long commentId,
        String adminNickname
) {
    public static WarningHistoryDto from(UserWarning warning) {
        return new WarningHistoryDto(
                warning.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                warning.getReason(),
                warning.getComment().getId(),
                warning.getAdmin().getNickname()
        );
    }
}
