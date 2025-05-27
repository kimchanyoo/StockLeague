package com.stockleague.backend.admin.service;

import com.stockleague.backend.admin.dto.request.AdminUserForceWithdrawRequestDto;
import com.stockleague.backend.admin.dto.response.AdminUserForceWithdrawResponseDto;
import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.infra.redis.TokenRedisService;
import com.stockleague.backend.notification.domain.NotificationType;
import com.stockleague.backend.notification.domain.TargetType;
import com.stockleague.backend.notification.dto.NotificationEvent;
import com.stockleague.backend.kafka.producer.NotificationProducer;
import com.stockleague.backend.stock.domain.Comment;
import com.stockleague.backend.stock.repository.CommentRepository;
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
    private final TokenRedisService tokenRedisService;
    private final CommentRepository commentRepository;

    @Transactional
    public AdminUserForceWithdrawResponseDto forceWithdrawUser(
            Long adminId, AdminUserForceWithdrawRequestDto requestDto) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        User user = userRepository.findById(requestDto.userId())
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        Comment comment = commentRepository.findById(requestDto.commentId())
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.COMMENT_NOT_FOUND));

        user.ban(requestDto.reason());
        comment.bannedByAdmin(admin);

        NotificationEvent event = new NotificationEvent(
                user.getId(),
                NotificationType.USER_BANNED,
                TargetType.USER,
                user.getId()
        );
        notificationProducer.send(event);

        tokenRedisService.deleteRefreshToken(requestDto.userId());

        return new AdminUserForceWithdrawResponseDto(true, "회원이 이용 정지되었습니다.");
    }
}