package com.stockleague.backend.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TokenReissueResponseDto {
    private boolean success;
    private String accessToken;

    @Builder
    public TokenReissueResponseDto(boolean success, String accessToken) {
        this.success = success;
        this.accessToken = accessToken;
    }
}
