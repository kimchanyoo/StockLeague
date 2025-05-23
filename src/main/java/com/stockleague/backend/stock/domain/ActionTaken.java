package com.stockleague.backend.stock.domain;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import java.util.Arrays;

public enum ActionTaken {
    NONE,
    REJECTED,
    WARNING,
    COMMENT_DELETED,
    BANNED;

    public static ActionTaken from(String actionTaken) {
        return Arrays.stream(ActionTaken.values())
                .filter(type -> type.name().equalsIgnoreCase(actionTaken))
                .findFirst()
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.INVALID_ACTION_TYPE));
    }
}
