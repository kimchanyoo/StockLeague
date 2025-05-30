package com.stockleague.backend.infra.webSocket;

import com.fasterxml.jackson.databind.ObjectMapper;
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

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(dto);
            log.info("보내는 WebSocket JSON: {}", json);
        } catch (Exception e) {
            log.error("WebSocket 전송 JSON 직렬화 실패", e);
        }

        log.info("WebSocket 알림 전송: userId={}, message={}", userId, dto.message());

        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "queue/notifications",
                dto
        );
    }
}
