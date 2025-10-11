package com.stockleague.backend.inquiry.dto.response;

import com.stockleague.backend.inquiry.domain.Inquiry;
import java.time.format.DateTimeFormatter;

public record InquiryDetailForUserResponseDto(
        Boolean success,
        Long inquiryId,
        String userNickname,
        String title,
        String category,
        String content,
        String status,
        String createdAt,
        String updatedAt,
        InquiryAnswerDto answers
) {
    public static InquiryDetailForUserResponseDto from(Inquiry inquiry, InquiryAnswerDto answer) {
        return new InquiryDetailForUserResponseDto(
                true,
                inquiry.getId(),
                inquiry.getNickname(),
                inquiry.getTitle(),
                inquiry.getCategory(),
                inquiry.getContent(),
                inquiry.getStatus().name(),
                inquiry.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                inquiry.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                answer
        );
    }
}
