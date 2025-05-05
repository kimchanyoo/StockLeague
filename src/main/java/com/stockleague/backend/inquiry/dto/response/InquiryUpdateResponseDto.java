package com.stockleague.backend.inquiry.dto.response;


public record InquiryUpdateResponseDto(
        Boolean success,
        String message
) {
    public static InquiryUpdateResponseDto from() {
        return new InquiryUpdateResponseDto(
                true,
                "문의가 성공적으로 수정되었습니다."
        );
    }
}
