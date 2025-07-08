package com.stockleague.backend.user.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.domain.UserAsset;
import com.stockleague.backend.user.dto.response.UserAssetResponseDto;
import com.stockleague.backend.user.repository.UserAssetRepository;
import com.stockleague.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAssetService {

    private final UserRepository userRepository;
    private final UserAssetRepository userAssetRepository;

    /**
     * 주어진 사용자 ID에 해당하는 자산 정보를 조회합니다.
     * <p>
     * - 사용자(User)가 존재하지 않으면 {@code GlobalErrorCode.USER_NOT_FOUND} 예외를 발생시킵니다.
     * - 사용자 자산(UserAsset)이 존재하지 않으면 {@code GlobalErrorCode.ASSET_NOT_FOUND} 예외를 발생시킵니다.
     * </p>
     *
     * @param userId 조회할 사용자의 ID
     * @return {@link UserAssetResponseDto} 사용자 자산 응답 DTO
     * @throws GlobalException 사용자 또는 자산 정보가 존재하지 않을 경우
     */
    @Transactional(readOnly = true)
    public UserAssetResponseDto getUserAsset(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        UserAsset asset = user.getUserAsset();

        if (asset == null) {
            throw new GlobalException(GlobalErrorCode.ASSET_NOT_FOUND);
        }

        return UserAssetResponseDto.from(asset);
    }
}
