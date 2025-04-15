package com.stockleague.backend.global.exception;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {
    private final GlobalErrorCode globalErrorCode;

    public GlobalException(GlobalErrorCode errorCode) {
        super(errorCode.getMessage());
        this.globalErrorCode = errorCode;
    }

    public GlobalErrorCode getErrorCode() {
        return globalErrorCode;
    }
}
