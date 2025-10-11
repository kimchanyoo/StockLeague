package com.stockleague.backend.inquiry.dto.request;

public record InquiryUpdateRequestDto(
        String title,
        String category,
        String content
) {
}
