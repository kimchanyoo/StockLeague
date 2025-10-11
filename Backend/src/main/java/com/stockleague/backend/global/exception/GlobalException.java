package com.stockleague.backend.global.exception;

import java.util.Map;
import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {
    private final GlobalErrorCode globalErrorCode;
    private final Map<String, Object> details;

    public GlobalException(GlobalErrorCode errorCode) {
        super(errorCode.getMessage());
        this.globalErrorCode = errorCode;
        this.details = Map.of();
    }

    public GlobalException(GlobalErrorCode errorCode, Map<String, Object> details) {
        super(errorCode.getMessage());
        this.globalErrorCode = errorCode;
        this.details = details;
    }

    public GlobalErrorCode getErrorCode() {
        return globalErrorCode;
    }
}
