package com.stockleague.backend.openapi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenApiTokenResponseDto(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        long expiresIn,  // 초 단위

        @JsonProperty("access_token_token_expired")
        String accessTokenTokenExpired
) {
}
