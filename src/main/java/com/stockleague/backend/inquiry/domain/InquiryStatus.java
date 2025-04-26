package com.stockleague.backend.inquiry.domain;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import java.util.Arrays;

public enum InquiryStatus {
    WAITING,
    ANSWERED;

    public static InquiryStatus from(String value) {
        return Arrays.stream(InquiryStatus.values())
                .filter(status -> status.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.INVALID_STATUS));
    }
}
