package com.stockleague.backend.global.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ErrorResponse {
    private final boolean success = false;
    private final String message;
    private final String errorCode;

    @Builder
    public ErrorResponse(String message, String errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }

    public static ErrorResponse of(GlobalErrorCode errorCode) {
        return ErrorResponse.builder()
                .message(errorCode.getMessage())
                .errorCode(errorCode.name())
                .build();
    }
}
