package com.stockleague.backend.notification.domain;

import lombok.Getter;

@Getter
public enum NotificationType {
    REPLY("새 댓글이 달렸습니다.", false),
    LIKE("회원님의 댓글이 좋아요를 받았습니다.", false),
    WARNING("회원님의 댓글이 신고로 인해 경고 처리되었습니다.", true),
    INQUIRY_ANSWER("문의에 답변이 등록되었습니다.", true),
    TRADE_PARTIALLY_EXECUTED("주문이 부분 체결되었습니다.", false),
    TRADE_EXECUTED("주문이 체결되었습니다.",true);

    private final String defaultMessage;
    private final boolean deduplicated;

    NotificationType(String defaultMessage, boolean deduplicated) {
        this.defaultMessage = defaultMessage;
        this.deduplicated = deduplicated;
    }
}
