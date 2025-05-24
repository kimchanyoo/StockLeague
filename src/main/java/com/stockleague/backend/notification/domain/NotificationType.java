package com.stockleague.backend.notification.domain;

import lombok.Getter;

@Getter
public enum NotificationType {
    REPLY("새 댓글이 달렸습니다.", false),
    LIKE("회원님의 댓글이 좋아요를 받았습니다.", false),
    INQUIRY_ANSWER("문의에 답변이 등록되었습니다.", true),
    TRADE_PARTIALLY_EXECUTED("주문이 부분 체결되었습니다.", false),
    TRADE_EXECUTED("주문이 체결되었습니다.",true),
    COMMENT_DELETED("댓글이 커뮤니티 가이드라인 위반으로 삭제되었습니다. 추후 유의해 주세요.", false),
    COMMENT_DELETED_AND_WARNED("댓글이 삭제되었고, 경고가 부여되었습니다.", true),
    USER_BANNED("회원님은 운영 정책에 따라 이용이 정지되었습니다.", true);

    private final String defaultMessage;
    private final boolean deduplicated;

    NotificationType(String defaultMessage, boolean deduplicated) {
        this.defaultMessage = defaultMessage;
        this.deduplicated = deduplicated;
    }
}
