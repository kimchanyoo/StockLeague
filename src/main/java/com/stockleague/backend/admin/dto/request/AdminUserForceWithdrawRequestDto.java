package com.stockleague.backend.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminUserForceWithdrawRequestDto(
        @NotBlank(message = "정지 사유를 입력해주세요.")
        String reason,

        @NotBlank(message = "댓글 ID를 입력해주세요.")
        Long commentId,

        @NotBlank(message = "정지될 유저 ID를 입력해주세요.")
        Long userId
) {
}
