package com.stockleague.backend.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminUserForceWithdrawRequestDto(
        @NotBlank(message = "강제 탈퇴 사유를 입력해주세요.")
        String reason
) {
}
