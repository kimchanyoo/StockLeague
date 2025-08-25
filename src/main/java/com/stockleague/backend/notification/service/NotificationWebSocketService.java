package com.stockleague.backend.notification.service;

import com.stockleague.backend.notification.dto.NotificationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 알림을 유저별로 전송하는 메소드
     * @param userId 유저 ID
     * @param dto 알림 관련 DTO
     */
    public void sendToUser(Long userId, NotificationResponseDto dto) {
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "/queue/notifications",
                dto
        );
    }
}
