package com.stockleague.backend.global.exception;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ErrorResponse {
    private final boolean success = false;
    private final String message;
    private final String errorCode;
    Map<String, Object> details;

    @Builder
    public ErrorResponse(String message, String errorCode, Map<String, Object> details) {
        this.message = message;
        this.errorCode = errorCode;
        this.details = details;
    }

    public static ErrorResponse of(GlobalErrorCode errorCode) {
        return ErrorResponse.builder()
                .message(errorCode.getMessage())
                .errorCode(errorCode.name())
                .build();
    }

    public static ErrorResponse of(GlobalException ex) {
        return ErrorResponse.builder()
                .message(ex.getErrorCode().getMessage())
                .errorCode(ex.getErrorCode().name())
                .details(ex.getDetails())
                .build();
    }
}
