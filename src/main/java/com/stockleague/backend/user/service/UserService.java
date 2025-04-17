package com.stockleague.backend.user.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.dto.request.UserProfileUpdateRequestDto;
import com.stockleague.backend.user.dto.request.UserWithdrawRequestDto;
import com.stockleague.backend.user.dto.response.UserProfileResponseDto;
import com.stockleague.backend.user.dto.response.UserProfileUpdateResponseDto;
import com.stockleague.backend.user.dto.response.UserWithdrawResponseDto;
import com.stockleague.backend.user.repository.UserRepository;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private static final Pattern nicknamePattern = Pattern.compile("^[a-zA-Z0-9가-힣]{2,10}$");

    public UserProfileResponseDto getUserProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        return new UserProfileResponseDto(
                true,
                "회원 정보를 가져오는데 성공했습니다.",
                user.getNickname()
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

        user.updateNickname(nickname);

        return new UserProfileUpdateResponseDto(true, "회원 정보가 수정되었습니다.", nickname);
    }

    @Transactional
    public UserWithdrawResponseDto deleteUser(Long id, UserWithdrawRequestDto request) {
        final String WITHDRAW_CONFIRM_MESSAGE = "탈퇴합니다.";

        User user = userRepository.findById(id)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        if (!WITHDRAW_CONFIRM_MESSAGE.equals(request.confirmMessage())) {
            throw new GlobalException(GlobalErrorCode.INVALID_WITHDRAW_CONFIRM_MESSAGE);
        }

        userRepository.delete(user);

        return new UserWithdrawResponseDto(true, "회원 탈퇴가 완료되었습니다.");
    }
}