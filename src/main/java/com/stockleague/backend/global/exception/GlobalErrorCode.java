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
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인입니다."),
    INVALID_REDIRECT_URI(HttpStatus.BAD_REQUEST, "잘못된 RedirectUri를 입력하였습니다."),
    BANNED_USER(HttpStatus.FORBIDDEN, "정지된 유저입니다."),
    DATABASE_CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST, "데이터 무결성 제약 조건에 위배되었습니다."),

    // 닉네임 형식
    NICKNAME_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "닉네임 형식이 올바르지 않습니다."),
    DUPLICATED_NICKNAME(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다."),

    // 문의 및 공지사항 작성 관련
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 문의를 찾을 수 없습니다."),
    INQUIRY_ALREADY_ANSWERED(HttpStatus.BAD_REQUEST, "이미 답변이 등록된 문의입니다."),
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 공지사항을 찾을 수 없습니다."),
    MISSING_KEYWORD(HttpStatus.BAD_REQUEST, "검색어를 입력해주세요."),
    INVALID_RESTORE_OPERATION(HttpStatus.BAD_REQUEST, "복원할 수 없는 상태입니다."),
    INVALID_STATUS(HttpStatus.BAD_REQUEST, "잘못된 문의 상태가 입력되었습니다."),

    // 페이지 처리
    INVALID_PAGINATION(HttpStatus.BAD_REQUEST, "페이지 번호 또는 크기가 유효하지 않습니다."),

    // 토큰
    INVALID_TEMP_TOKEN(HttpStatus.UNAUTHORIZED, "임시 토큰이 유효하지 않습니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "엑세스 토큰이 유효하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "리프레시 토큰이 유효하지 않습니다."),

    // 유저 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    INVALID_WITHDRAW_CONFIRM_MESSAGE(HttpStatus.BAD_REQUEST, "탈퇴 문구가 일치하지 않습니다."),
    ASSET_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저의 자산 정보가 존재하지 않습니다."),

    // 주식 관련
    INVALID_MARKET_TYPE(HttpStatus.BAD_REQUEST, "잘못된 시장 유형이 입력되었습니다."),
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 종목을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    INVALID_COMMENT_OWNER(HttpStatus.UNAUTHORIZED, "자신이 작성한 댓글만 관리할 수 있습니다."),
    INVALID_TARGET_TYPE(HttpStatus.BAD_REQUEST, "잘못된 신고 대상이 입력되었습니다."),
    INVALID_STATUS_TYPE(HttpStatus.BAD_REQUEST, "잘못된 진행 상태가 입력되었습니다."),
    INVALID_ACTION_TYPE(HttpStatus.BAD_REQUEST, "잘못된 처리 결과가 입력되었습니다."),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 신고를 찾을 수 없습니다."),

    // 잘못된 필드값 입력
    MISSING_FIELDS(HttpStatus.BAD_REQUEST, "내용을 모두 입력해야 합니다."),

    // 알림 관련
    INVALID_NOTIFICATION_TYPE(HttpStatus.BAD_REQUEST, "잘못된 알림 유형이 입력되었습니다."),

    // 관심 종목 관련
    WATCHLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 관심 종목을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    GlobalErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}

