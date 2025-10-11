package com.stockleague.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdditionalInfoRequestDto(
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,

        @NotNull(message = "약관 동의 여부는 필수입니다.")
        Boolean agreedToTerms,

        @NotNull(message = "15세 이상 여부는 필수입니다.")
        Boolean isOverFifteen
) {
}
