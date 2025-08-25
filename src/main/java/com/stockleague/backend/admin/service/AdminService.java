package com.stockleague.backend.admin.service;

import com.stockleague.backend.admin.dto.request.AdminUserForceWithdrawRequestDto;
import com.stockleague.backend.admin.dto.response.AdminUserForceWithdrawResponseDto;
import com.stockleague.backend.admin.dto.response.NewUserCountResponseDto;
import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.infra.redis.TokenRedisService;
import com.stockleague.backend.notification.domain.NotificationType;
import com.stockleague.backend.notification.domain.TargetType;
import com.stockleague.backend.notification.dto.NotificationEvent;
import com.stockleague.backend.notification.service.NotificationService;
import com.stockleague.backend.stock.domain.Comment;
import com.stockleague.backend.stock.repository.CommentRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final TokenRedisService tokenRedisService;
    private final CommentRepository commentRepository;

    private final NotificationService notificationService;

    private final int newUserCountWithinDays = 7;

    @Transactional
    public AdminUserForceWithdrawResponseDto forceWithdrawUser(
            AdminUserForceWithdrawRequestDto requestDto, Long adminId, Long userId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        Comment comment = commentRepository.findById(requestDto.commentId())
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.COMMENT_NOT_FOUND));

        user.ban(requestDto.reason());
        comment.bannedByAdmin(admin);

        notificationService.notify(new NotificationEvent(
                user.getId(),
                NotificationType.USER_BANNED,
                TargetType.USER,
                user.getId()
        ));

        tokenRedisService.deleteRefreshToken(userId);

        return new AdminUserForceWithdrawResponseDto(true, "회원이 이용 정지되었습니다.");
    }

    public NewUserCountResponseDto countNewUsers() {
        LocalDateTime daysAgo = LocalDateTime.now().minusDays(newUserCountWithinDays);

        return new NewUserCountResponseDto(
                true,
                userRepository.countByCreatedAtAfter(daysAgo)
        );
    }
}