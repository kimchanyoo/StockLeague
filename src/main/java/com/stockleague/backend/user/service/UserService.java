package com.stockleague.backend.user.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.infra.redis.TokenRedisService;
import com.stockleague.backend.infra.redis.UserRedisCleanupService;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.dto.request.UserProfileUpdateRequestDto;
import com.stockleague.backend.user.dto.request.UserWithdrawRequestDto;
import com.stockleague.backend.user.dto.response.UserProfileResponseDto;
import com.stockleague.backend.user.dto.response.UserProfileUpdateResponseDto;
import com.stockleague.backend.user.dto.response.UserWithdrawResponseDto;
import com.stockleague.backend.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRedisCleanupService userRedisCleanupService;
    private final TokenRedisService tokenRedisService;
    private final PlatformTransactionManager txm;

    private static final Pattern nicknamePattern = Pattern.compile("^[a-zA-Z0-9가-힣]{2,10}$");

    public UserProfileResponseDto getUserProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        return new UserProfileResponseDto(
                true,
                "회원 정보를 가져오는데 성공했습니다.",
                user.getNickname(),
                user.getRole().toString()
        );
    }

    @Transactional
    public UserProfileUpdateResponseDto updateUserProfile(Long id,
                                                          UserProfileUpdateRequestDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        String nickname = request.nickname();
        if (!nicknamePattern.matcher(nickname).matches()) {
            throw new GlobalException(GlobalErrorCode.NICKNAME_FORMAT_INVALID);
        }

        if (userRepository.existsByNickname(nickname)) {
            throw new GlobalException(GlobalErrorCode.DUPLICATED_NICKNAME);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last = user.getLastNicknameChangedAt();
        if (last != null) {
            LocalDateTime nextAvailable = last.plusDays(30);
            if (now.isBefore(nextAvailable)) {
                long daysLeft = ChronoUnit.DAYS.between(now, nextAvailable);
                throw new GlobalException(
                        GlobalErrorCode.NICKNAME_CHANGE_NOT_ALLOWED,
                        Map.of("daysLeft", daysLeft, "nextAvailableAt", nextAvailable)
                );
            }
        }

        user.updateNickname(nickname);
        user.setLastNicknameChangedAt(now);

        LocalDateTime nextAvailable = now.plusDays(30);

        return new UserProfileUpdateResponseDto(
                true,
                "회원 정보가 수정되었습니다.",
                nickname,
                now,
                nextAvailable
        );
    }

    @Transactional
    public UserWithdrawResponseDto deleteUser(Long id, UserWithdrawRequestDto request) {
        final String WITHDRAW_CONFIRM_MESSAGE = "탈퇴합니다.";

        if (!WITHDRAW_CONFIRM_MESSAGE.equals(request.confirmMessage())) {
            throw new GlobalException(GlobalErrorCode.INVALID_WITHDRAW_CONFIRM_MESSAGE);
        }

        TransactionTemplate requiresNew = new TransactionTemplate(txm);
        requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        TransactionTemplate required = new TransactionTemplate(txm);
        required.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        requiresNew.executeWithoutResult(status -> {
            int updated = userRepository.markDeleting(id);
            if (updated == 0) throw new GlobalException(GlobalErrorCode.USER_NOT_FOUND);
        });

        tokenRedisService.deleteRefreshToken(id);

        userRedisCleanupService.purgeAllForUser(id);

        required.executeWithoutResult(status -> userRepository.deleteById(id));

        return new UserWithdrawResponseDto(true, "회원 탈퇴가 완료되었습니다.");
    }
}