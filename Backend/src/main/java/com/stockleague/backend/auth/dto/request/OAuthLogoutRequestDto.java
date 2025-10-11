package com.stockleague.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OAuthLogoutRequestDto(
        @NotBlank(message = "Refresh Token은 필수입니다.")
        String refreshToken
) {
}
