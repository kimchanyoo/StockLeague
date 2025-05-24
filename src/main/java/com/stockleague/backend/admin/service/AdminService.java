package com.stockleague.backend.admin.service;

import com.stockleague.backend.admin.dto.request.AdminUserForceWithdrawRequestDto;
import com.stockleague.backend.admin.dto.response.AdminUserForceWithdrawResponseDto;
import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.notification.domain.NotificationType;
import com.stockleague.backend.notification.domain.TargetType;
import com.stockleague.backend.notification.dto.NotificationEvent;
import com.stockleague.backend.notification.kafka.producer.NotificationProducer;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final NotificationProducer notificationProducer;

    @Transactional
    public AdminUserForceWithdrawResponseDto forceWithdrawUser(
            Long userId, AdminUserForceWithdrawRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        user.ban();

        NotificationEvent event = new NotificationEvent(
                user.getId(),
                NotificationType.USER_BANNED,
                TargetType.USER,
                user.getId()
        );
        notificationProducer.send(event);

        return new AdminUserForceWithdrawResponseDto(true, "회원이 이용 정지되었습니다.");
    }
}
