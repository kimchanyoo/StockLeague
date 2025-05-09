package com.stockleague.backend.global.handler;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TokenCookieHandler {

    private static final String ACCESS_TOKEN_NAME = "access_token";
    private static final String REFRESH_TOKEN_NAME = "refresh_token";

    private static final Duration ACCESS_TOKEN_EXPIRE = Duration.ofMinutes(30);
    private static final Duration REFRESH_TOKEN_EXPIRE = Duration.ofDays(30);

    public void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN_NAME, accessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(ACCESS_TOKEN_EXPIRE)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(REFRESH_TOKEN_EXPIRE)
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        log.info("[쿠키 설정] access_token 및 refresh_token 쿠키가 설정되었습니다.");
    }

    public void removeTokenCookies(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        log.info("[쿠키 제거] access_token 및 refresh_token 쿠키가 제거되었습니다.");
    }
}
