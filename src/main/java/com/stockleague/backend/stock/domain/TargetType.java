package com.stockleague.backend.stock.domain;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import java.util.Arrays;

public enum TargetType {
    COMMENT,
    REPLY;

    public static TargetType from(String targetType) {
        return Arrays.stream(TargetType.values())
                .filter(type -> type.name().equalsIgnoreCase(targetType))
                .findFirst()
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.INVALID_TARGET_TYPE));
    }
}
