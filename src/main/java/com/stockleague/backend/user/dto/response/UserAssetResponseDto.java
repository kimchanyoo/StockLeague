package com.stockleague.backend.user.dto.response;

import com.stockleague.backend.user.domain.UserAsset;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public record UserAssetResponseDto(
        boolean success,
        String message,
        Long userId,
        BigDecimal cashBalance,
        BigDecimal totalValuation,
        String updatedAt
) {
    public static UserAssetResponseDto from(UserAsset userAsset) {
        return new UserAssetResponseDto(
                true,
                "회원 자산 정보를 가져오는데 성공했습니다.",
                userAsset.getUserId(),
                userAsset.getCashBalance(),
                userAsset.getTotalValuation(),
                userAsset.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
