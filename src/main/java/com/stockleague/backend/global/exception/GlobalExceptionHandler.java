package com.stockleague.backend.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ErrorResponse> handleGlobal(GlobalException ex) {
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(ErrorResponse.of(ex));
    }

    // 예외를 못잡은 경우 대비
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("[Global] 예기치 못한 예외 발생", e);
        return ResponseEntity
                .status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorResponse.of(GlobalErrorCode.INTERNAL_SERVER_ERROR));
    }
}
