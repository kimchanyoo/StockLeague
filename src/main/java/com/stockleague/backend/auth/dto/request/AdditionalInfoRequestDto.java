package com.stockleague.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AdditionalInfoRequestDto {

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;

    @NotNull(message = "약관 동의 여부는 필수입니다.")
    private Boolean agreedToTerms;

    @NotNull(message = "15세 이상 여부는 필수입니다.")
    private Boolean isOverFifteen;
}
