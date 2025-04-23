package com.stockleague.backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum GlobalErrorCode {

    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    //소셜 로그인
    OAUTH_AUTH_FAILED(HttpStatus.BAD_REQUEST, "소셜 로그인 인증에 실패했습니다."),
    OAUTH_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "소셜 로그인 서버와 통신 중 문제가 발생했습니다."),
    AGE_RESTRICTION(HttpStatus.BAD_REQUEST, "15세 미만은 가입할 수 없습니다."),
    ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "이미 가입된 유저입니다."),

    // 닉네임 형식
    NICKNAME_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "닉네임 형식이 올바르지 않습니다."),
    DUPLICATED_NICKNAME(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다."),

    // 공지사항 관련
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 공지사항을 찾을 수 없습니다."),
    MISSING_FIELDS(HttpStatus.BAD_REQUEST, "제목과 내용을 모두 입력해야 합니다."),

    // 토큰
    INVALID_TEMP_TOKEN(HttpStatus.UNAUTHORIZED, "임시 토큰이 유효하지 않습니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "엑세스 토큰이 유효하지 않습니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "엑세스 토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "리프레시 토큰이 유효하지 않습니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),

    // 유저 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    INVALID_WITHDRAW_CONFIRM_MESSAGE(HttpStatus.BAD_REQUEST, "탈퇴 문구가 일치하지 않습니다.");

    private final HttpStatus status;
    private final String message;

    GlobalErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}

