package com.stockleague.backend.stock.domain;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import java.util.Arrays;

public enum Status {
    PENDING,
    REJECTED,
    WARNING_SENT,
    COMMENT_DELETED;

    public static Status from(String status) {
        return Arrays.stream(Status.values())
                .filter(type -> type.name().equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.INVALID_STATUS_TYPE));
    }
}
