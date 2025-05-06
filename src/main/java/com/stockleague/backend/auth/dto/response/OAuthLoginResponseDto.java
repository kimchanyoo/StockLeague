package com.stockleague.backend.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OAuthLoginResponseDto {
    private boolean success;
    private String message;
    private boolean isFirstLogin;
    private String accessToken;
    private String refreshToken;
    private String role;

    @Builder
    public OAuthLoginResponseDto(boolean success, String message, boolean firstLogin, String accessToken,
                                 String refreshToken, String role) {
        this.success = success;
        this.message = message;
        this.isFirstLogin = firstLogin;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
    }
}
