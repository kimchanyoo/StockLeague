package com.stockleague.backend.inquiry.dto.response;

import com.stockleague.backend.inquiry.domain.Inquiry;
import java.time.format.DateTimeFormatter;

public record InquiryDeleteResponseDto(
        Boolean success,
        String message,
        String deletedAt
) {
    public static InquiryDeleteResponseDto from(Inquiry inquiry) {
        return new InquiryDeleteResponseDto(
                true,
                "문의가 성공적으로 삭제되었습니다.",
                inquiry.getDeletedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
