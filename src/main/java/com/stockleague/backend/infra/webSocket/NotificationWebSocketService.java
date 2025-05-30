package com.stockleague.backend.infra.webSocket;

import com.stockleague.backend.notification.domain.Notification;
import com.stockleague.backend.notification.dto.NotificationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(Long userId, Notification notification) {
        NotificationResponseDto dto = NotificationResponseDto.from(notification);

        log.info("WebSocket 알림 전송: userId={}, message={}", userId, dto.message());

        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "queue/notifications",
                dto
        );
    }
}
