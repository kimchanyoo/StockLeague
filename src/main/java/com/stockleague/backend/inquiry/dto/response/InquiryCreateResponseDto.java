package com.stockleague.backend.inquiry.dto.response;

public record InquiryCreateResponseDto(
        Boolean success,
        String message,
        Long inquiryId
) {
}