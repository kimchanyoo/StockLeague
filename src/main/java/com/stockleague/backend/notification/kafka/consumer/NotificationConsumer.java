package com.stockleague.backend.notification.kafka.consumer;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.notification.domain.Notification;
import com.stockleague.backend.notification.dto.NotificationEvent;
import com.stockleague.backend.notification.repository.NotificationRepository;
import com.stockleague.backend.notification.service.NotificationWebSocketService;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationWebSocketService webSocketService;

    @KafkaListener(
            topics = "user-notification",
            groupId = "notification-group",
            containerFactory = "notificationListenerContainerFactory"
    )
    public void consume(NotificationEvent event) {
        log.info("Kafka 알림 수신: userId={}, type={}, target={}, targetId={}",
                event.userId(), event.type(), event.target(), event.targetId());

        User user = userRepository.findById(event.userId())
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        boolean isDuplicate = event.type().isDeduplicated() &&
                notificationRepository.existsByUserAndTypeAndTargetIdAndIsDeletedFalse(
                        user, event.type(), event.targetId()
                );

        if (!isDuplicate) {
            Notification notification = Notification.builder()
                    .user(user)
                    .type(event.type())
                    .target(event.target())
                    .targetId(event.targetId())
                    .message(event.resolvedMessage())
                    .isRead(false)
                    .isDeleted(false)
                    .build();

            notificationRepository.save(notification);

            webSocketService.sendNotification(user.getId(), notification);
        } else {
            log.info("중복 알림 감지됨 - 저장 생략");
        }
    }
}
