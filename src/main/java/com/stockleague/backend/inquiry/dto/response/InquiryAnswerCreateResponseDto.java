package com.stockleague.backend.inquiry.dto.response;

import com.stockleague.backend.inquiry.domain.InquiryAnswer;
import com.stockleague.backend.inquiry.domain.InquiryStatus;

public record InquiryAnswerCreateResponseDto(
        Boolean success,
        String message,
        Long answerId,
        String status
) {
    public static InquiryAnswerCreateResponseDto from(InquiryAnswer answer) {
        return new InquiryAnswerCreateResponseDto(
                true,
                "답변이 등록되었습니다.",
                answer.getId(),
                InquiryStatus.ANSWERED.name()
        );
    }
}
