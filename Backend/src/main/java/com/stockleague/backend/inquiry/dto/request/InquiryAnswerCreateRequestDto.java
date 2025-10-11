package com.stockleague.backend.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;

public record InquiryAnswerCreateRequestDto(

        @NotBlank(message = "답변 내용은 필수입니다.")
        String content
) {
}
