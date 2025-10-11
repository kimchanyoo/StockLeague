package com.stockleague.backend.notification.repository;

import com.stockleague.backend.notification.domain.Notification;
import com.stockleague.backend.notification.domain.NotificationType;
import com.stockleague.backend.notification.domain.TargetType;
import com.stockleague.backend.user.domain.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 지정된 사용자/타입/타겟/타겟ID에 해당하는 "삭제되지 않은" 알림이 존재하는지 여부를 확인합니다.
     * <p>
     * - 주로 {@link NotificationType#isDeduplicated()} 가 true인 알림 중복 방지를 위해 사용됩니다.<br>
     * - targetId 가 null인 경우에는 "IS NULL" 조건으로 조회됩니다.
     *
     * @param user      알림을 받을 사용자 엔티티
     * @param type      알림 타입
     * @param target    알림 타겟 도메인(COMMENT, USER, INQUIRY, TRADE 등)
     * @param targetId  알림 타겟 ID (없으면 null)
     * @return          동일 조건의 알림이 존재하면 true, 없으면 false
     */
    boolean existsByUserAndTypeAndTargetAndTargetIdAndIsDeletedFalse(
            User user, NotificationType type, TargetType target, Long targetId
    );

    /**
     * 지정된 사용자/타입/타겟/타겟ID 조건에 해당하는 가장 최근의 "삭제되지 않은" 알림을 조회합니다.
     * <p>
     * - 정렬 기준: notification_id 내림차순 (가장 마지막에 저장된 알림 1건)<br>
     * - deduplicated 알림이 이미 존재할 때 최신 알림 DTO 반환 등에 활용됩니다.
     *
     * @param user      알림을 받을 사용자 엔티티
     * @param type      알림 타입
     * @param target    알림 타겟 도메인(COMMENT, USER, INQUIRY, TRADE 등)
     * @param targetId  알림 타겟 ID (없으면 null)
     * @return          Optional<Notification> (존재하지 않으면 Optional.empty)
     */
    Optional<Notification> findTop1ByUserAndTypeAndTargetAndTargetIdAndIsDeletedFalseOrderByIdDesc(
            User user, NotificationType type, TargetType target, Long targetId
    );

    @Query("""
           select n from Notification n
            where n.user.id = :userId
              and n.isDeleted = false
              and (
                   :status = 'all'
                or (:status = 'unread' and n.isRead = false)
                or (:status = 'read'   and n.isRead = true)
              )
           """)
    Page<Notification> findByUserAndStatus(@Param("userId") Long userId,
                                           @Param("status") String status,
                                           Pageable pageable);

    long countByUser_IdAndIsReadFalseAndIsDeletedFalse(Long userId);

    Notification findByIdAndUser_IdAndIsDeletedFalse(Long id, Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update Notification n
              set n.isRead = true,
                  n.updatedAt = CURRENT_TIMESTAMP
            where n.user.id = :userId
              and n.isDeleted = false
              and n.isRead = false
              and (:target is null or n.target = :target)
           """)
    int markAllAsRead(@Param("userId") Long userId, @Param("target") TargetType target);

    @Modifying
    @Query("""
           delete from Notification n
            where n.isDeleted = true
              and n.deletedAt < :threshold
           """)
    int purgeDeletedBefore(@Param("threshold") LocalDateTime threshold);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
       update Notification n
          set n.isDeleted = true,
              n.deletedAt = CURRENT_TIMESTAMP
        where n.id = :id
          and n.user.id = :userId
          and n.isDeleted = false
       """)
    int softClose(@Param("userId") Long userId, @Param("id") Long id);
}
