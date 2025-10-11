package com.stockleague.backend.global.handler;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TokenCookieHandler {

    private static final String REFRESH_TOKEN_NAME = "refresh_token";

    private static final Duration REFRESH_TOKEN_EXPIRE = Duration.ofDays(30);

    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(REFRESH_TOKEN_EXPIRE)
                .build();

        response.addHeader("Set-Cookie", refreshCookie.toString());

        log.info("[쿠키 설정] refresh_token 쿠키가 설정되었습니다.");
    }

    public void removeRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", refreshCookie.toString());

        log.info("[쿠키 제거] refresh_token 쿠키가 제거되었습니다.");
    }
}
