package com.stockleague.backend.inquiry.dto.response;

import java.util.List;

public record InquiryPageResponseDto(
        Boolean success,
        List<InquirySummaryDto> inquiries,
        int page,
        int size,
        long totalCount
) {
}
