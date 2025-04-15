package com.stockleague.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class OAuthLoginRequestDto {

    @NotBlank(message = "소셜 로그인 제공자(provider)는 필수입니다.")
    private String provider;

    @NotBlank(message = "인가 코드(authCode)는 필수입니다.")
    private String authCode;
}
