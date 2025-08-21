package com.stockleague.backend.notification.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.notification.domain.Notification;
import com.stockleague.backend.notification.domain.TargetType;
import com.stockleague.backend.notification.dto.NotificationEvent;
import com.stockleague.backend.notification.dto.NotificationListResponseDto;
import com.stockleague.backend.notification.dto.NotificationResponseDto;
import com.stockleague.backend.notification.repository.NotificationRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.repository.UserRepository;
import io.micrometer.common.lang.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    private static final List<String> ALLOWED_STATUS = List.of("unread", "read", "all");

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

    /**
     * 알림 목록 조회 (페이지네이션 지원).
     *
     * <p>조회 조건:
     * - status: unread | read | all (기타 값은 INVALID_PARAM 예외 발생)
     * <p>- page, size: 1 이상 (아닐 경우 INVALID_PAGINATION 예외 발생)</p>
     *
     *<p>처리 흐름</p>
     * <li>status 유효성 검증 및 normalize</li>
     * <li>PageRequest 생성 (createdAt DESC 정렬)</li>
     * <li>Repository 조회 후 DTO 매핑</li>
     * <li>페이지 응답 DTO로 변환 반환</li>
     *
     * @param userId 조회할 사용자 ID
     * @param status 조회 상태(unread/read/all)
     * @param page   요청 페이지 번호 (1부터 시작)
     * @param size   페이지 크기
     * @return 알림 목록 페이지 응답 DTO
     */
    public NotificationListResponseDto getNotifications(Long userId, String status, int page, int size) {
        if (page < 1 || size < 1) {
            throw new GlobalException(GlobalErrorCode.INVALID_PAGINATION);
        }

        String st = normalizeStatus(status);
        if (!ALLOWED_STATUS.contains(st)) {
            throw new GlobalException(GlobalErrorCode.INVALID_PARAM);
        }

        PageRequest pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<NotificationResponseDto> result = notificationRepository
                .findByUserAndStatus(userId, st, pageable)
                .map(NotificationResponseDto::from);

        return new NotificationListResponseDto(
                true,
                result.getContent(),
                page,
                size,
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    /**
     * 읽지 않은 알림 개수 조회
     * @param userId 사용자 ID
     * @return 읽지 않은 알림 수
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUser_IdAndIsReadFalseAndIsDeletedFalse(userId);
    }

    /**
     * 알림 단건 읽음 처리
     * @param userId 사용자 ID
     * @param id 알림 ID
     * @throws GlobalException NOTIFICATION_NOT_FOUND 알림이 없거나 삭제된 경우
     */
    @Transactional
    public void markRead(Long userId, Long id) {
        Notification n = notificationRepository.findByIdAndUser_IdAndIsDeletedFalse(id, userId);
        if (n == null) {
            throw new GlobalException(GlobalErrorCode.NOTIFICATION_NOT_FOUND);
        }
        n.markAsRead();
    }

    /**
     * 알림 전체 읽음 처리 (target 필터 가능)
     * @param userId 사용자 ID
     * @param target 타겟 타입 (null 시 전체)
     * @return 업데이트된 알림 수
     */
    @Transactional
    public int markAllRead(Long userId, TargetType target) {
        return notificationRepository.markAllAsRead(userId, target);
    }

    /**
     * 알림 닫기(Soft Delete)
     * @param userId 사용자 ID
     * @param id 알림 ID
     * @throws GlobalException NOTIFICATION_NOT_FOUND 알림이 없거나 이미 삭제된 경우
     * @throws GlobalException FORBIDDEN 다른 사용자 소유인 경우
     */
    @Transactional
    public void close(Long userId, Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.NOTIFICATION_NOT_FOUND));

        if (!n.getUser().getId().equals(userId)) {
            throw new GlobalException(GlobalErrorCode.FORBIDDEN);
        }
        if (n.isDeleted()) {
            throw new GlobalException(GlobalErrorCode.NOTIFICATION_NOT_FOUND);
        }
        n.close();
    }

    /**
     * 보관 기간이 지난 알림 실제 삭제(Hard Delete)
     * @param threshold 기준 시각 (deletedAt < threshold)
     * @return 삭제된 알림 수
     */
    @Transactional
    public int purgeAll(LocalDateTime threshold) {
        return notificationRepository.purgeDeletedBefore(threshold);
    }

    private String normalizeStatus(String status) {
        return status == null ? "unread" : status.toLowerCase(Locale.ROOT);
    }
}
