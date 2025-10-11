package com.stockleague.backend.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserProfileUpdateRequestDto(
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname
) {
}
