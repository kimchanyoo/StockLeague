package com.stockleague.backend.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InquiryCreateRequestDto(

        @NotBlank(message = "문의 제목은 필수입니다.")
        @Size(max = 200, message = "제목은 최대 200자까지 가능합니다.")
        String title,

        @NotBlank(message = "문의 유형은 필수입니다.")
        @Size(max = 50, message = "문의 유형은 최대 50자까지 가능합니다.")
        String category,

        @NotBlank(message = "문의 내용은 필수입니다.")
        String content
) {
}
