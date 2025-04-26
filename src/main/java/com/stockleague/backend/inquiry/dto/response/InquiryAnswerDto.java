package com.stockleague.backend.inquiry.dto.response;

import com.stockleague.backend.inquiry.domain.InquiryAnswer;
import java.time.format.DateTimeFormatter;

public record InquiryAnswerDto(
        Long answerId,
        String content,
        String answeredAt
) {
    public static InquiryAnswerDto from(InquiryAnswer answer) {
        return new InquiryAnswerDto(
                answer.getId(),
                answer.getContent(),
                answer.getAnsweredAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
