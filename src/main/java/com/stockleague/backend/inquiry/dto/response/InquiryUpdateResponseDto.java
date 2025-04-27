package com.stockleague.backend.inquiry.dto.response;

import com.stockleague.backend.inquiry.domain.Inquiry;

public record InquiryUpdateResponseDto(
        Boolean success,
        String message
) {
    public static InquiryUpdateResponseDto from(Inquiry inquiry) {
        return new InquiryUpdateResponseDto(
                true,
                "문의가 성공적으로 수정되었습니다."
        );
    }
}
