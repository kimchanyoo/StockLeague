package com.stockleague.backend.notification.repository;

import com.stockleague.backend.notification.domain.Notification;
import com.stockleague.backend.notification.domain.NotificationType;
import com.stockleague.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByUserAndTypeAndTargetIdAndIsDeletedFalse(User user, NotificationType type, Long targetId);
}
