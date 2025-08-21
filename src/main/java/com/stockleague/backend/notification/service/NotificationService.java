package com.stockleague.backend.notification.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.notification.domain.Notification;
import com.stockleague.backend.notification.dto.NotificationEvent;
import com.stockleague.backend.notification.dto.NotificationResponseDto;
import com.stockleague.backend.notification.repository.NotificationRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.repository.UserRepository;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationWebSocketService wsService;

    /**
     * 이벤트를 기반으로 알림을 생성하고, 커밋 후 개인 WebSocket 큐로 전송합니다.
     * <p>
     * - {@code NotificationType.isDeduplicated() == true} 인 경우
     *   동일 (user, type, target, targetId) 조합의 알림이 이미 존재하면 새로 생성하지 않고,
     *   가장 최근 알림을 반환합니다.<br>
     * - messageOverride 가 전달되면 기본 메시지 대신 해당 메시지를 저장합니다.<br>
     * - DB 저장은 트랜잭션 내에서 수행되며, WebSocket 푸시는 afterCommit 시점에 전송됩니다.
     *
     * @param event           알림 생성 이벤트 (userId, type, target, targetId 포함)
     * @param messageOverride 기본 메시지를 대체할 사용자 정의 메시지 (nullable)
     * @return 생성되었거나 이미 존재하는 알림의 응답 DTO
     * @throws GlobalException USER_NOT_FOUND : userId에 해당하는 사용자가 없는 경우
     */
    @Transactional
    public NotificationResponseDto notify(NotificationEvent event, @Nullable String messageOverride) {
        User user = userRepository.findById(event.userId())
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        if (event.type().isDeduplicated()) {
            boolean exists = notificationRepository.existsByUserAndTypeAndTargetAndTargetIdAndIsDeletedFalse(
                    user, event.type(), event.target(), event.targetId()
            );
            if (exists) {
                log.info("[Notification] dedup skip: user={}, type={}, target={}, targetId={}",
                        user.getId(), event.type(), event.target(), event.targetId());
                return notificationRepository
                        .findTop1ByUserAndTypeAndTargetAndTargetIdAndIsDeletedFalseOrderByIdDesc(
                                user, event.type(), event.target(), event.targetId())
                        .map(NotificationResponseDto::from)
                        .orElseGet(() -> new NotificationResponseDto(
                                null, event.type().name(),
                                resolveMessage(event, messageOverride),
                                event.target().name(), event.targetId(),
                                false, null
                        ));
            }
        }

        Notification saved = notificationRepository.save(
                Notification.builder()
                        .user(user)
                        .type(event.type())
                        .target(event.target())
                        .targetId(event.targetId())
                        .message(resolveMessage(event, messageOverride))
                        .isRead(false)
                        .isDeleted(false)
                        .build()
        );

        NotificationResponseDto dto = NotificationResponseDto.from(saved);

        runAfterCommit(() -> {
            try {
                wsService.sendToUser(user.getId(), dto);
            } catch (Exception e) {
                log.warn("[Notification] websocket push failed userId={}, id={}", user.getId(), saved.getId(), e);
            }
        });

        return dto;
    }

    /**
     * 이벤트를 기반으로 알림을 생성하고, 커밋 후 개인 WebSocket 큐로 전송합니다.
     * <p>
     * messageOverride 없이 {@link NotificationEvent#resolvedMessage()} 값이 사용됩니다.
     *
     * @param event 알림 생성 이벤트
     * @return 생성되었거나 이미 존재하는 알림의 응답 DTO
     * @throws GlobalException USER_NOT_FOUND : userId에 해당하는 사용자가 없는 경우
     */
    @Transactional
    public NotificationResponseDto notify(NotificationEvent event) {
        return notify(event, null);
    }

    /**
     * 알림 메시지를 최종 결정합니다.
     * <p>
     * - messageOverride 값이 있으면 우선 사용<br>
     * - 없으면 {@link NotificationEvent#resolvedMessage()} 사용
     *
     * @param event    알림 이벤트
     * @param override 사용자 정의 메시지 (nullable)
     * @return 최종 저장될 메시지 문자열
     */
    private static String resolveMessage(NotificationEvent event, @Nullable String override) {
        return (override != null && !override.isBlank()) ? override : event.resolvedMessage();
    }

    /**
     * 트랜잭션 커밋 이후 실행할 작업을 등록합니다.
     * <p>
     * - 트랜잭션이 활성화된 경우 afterCommit 콜백에 등록<br>
     * - 트랜잭션이 없는 경우 즉시 실행
     *
     * @param task 커밋 후 실행할 Runnable 작업
     */
    private static void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override public void afterCommit() { task.run(); }
                    }
            );
        } else {
            task.run();
        }
    }
}
