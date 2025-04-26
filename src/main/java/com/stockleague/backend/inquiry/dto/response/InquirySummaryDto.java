package com.stockleague.backend.inquiry.dto.response;

import com.stockleague.backend.inquiry.domain.Inquiry;
import java.time.format.DateTimeFormatter;

public record InquirySummaryDto(
        Long inquiryId,
        String userNickname,
        String category,
        String title,
        String status,
        String createdAt,
        String updatedAt
) {
    public static InquirySummaryDto from(Inquiry inquiry) {
        return new InquirySummaryDto(
                inquiry.getId(),
                inquiry.getNickname(),
                inquiry.getCategory(),
                inquiry.getTitle(),
                inquiry.getStatus().name(),
                inquiry.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                inquiry.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
