package com.stockleague.backend.infra.webSocket;

import com.stockleague.backend.notification.domain.Notification;
import com.stockleague.backend.notification.dto.NotificationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(Long userId, Notification notification) {
        NotificationResponseDto dto = NotificationResponseDto.from(notification);

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                dto
        );
    }
}
