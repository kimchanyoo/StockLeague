package com.stockleague.backend.auth.service;

import com.stockleague.backend.auth.dto.request.AdditionalInfoRequestDto;
import com.stockleague.backend.auth.dto.response.OAuthLoginResponseDto;
import com.stockleague.backend.auth.dto.response.OAuthLogoutResponseDto;
import com.stockleague.backend.auth.jwt.JwtProvider;
import com.stockleague.backend.auth.jwt.JwtProvider.OauthTokenPayload;
import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.infra.redis.TokenRedisService;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.domain.UserRole;
import com.stockleague.backend.user.dto.response.NicknameCheckResponseDto;
import com.stockleague.backend.user.repository.UserRepository;
import java.time.Duration;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final TokenRedisService redisService;
    private static final Pattern nicknamePattern = Pattern.compile("^[a-zA-Z0-9가-힣]{2,10}$");

    public OAuthLoginResponseDto completeSignup(String tempToken, AdditionalInfoRequestDto requestDto) {

        OauthTokenPayload payload = jwtProvider.parseTempToken(tempToken);
        String oauthId = payload.oauthId();
        var provider = payload.provider();


        if (userRepository.findByOauthIdAndProvider(oauthId, provider).isPresent()) {
            throw new GlobalException(GlobalErrorCode.ALREADY_REGISTERED);
        }

        try {
            User user = userRepository.save(User.builder()
                    .oauthId(oauthId)
                    .provider(provider)
                    .nickname(requestDto.getNickname())
                    .role(UserRole.USER)
                    .agreedToTerms(requestDto.getAgreedToTerms())
                    .isOverFifteen(requestDto.getIsOverFifteen())
                    .build()
            );

            String accessToken = jwtProvider.createAccessToken(user.getId());
            String refreshToken = jwtProvider.createRefreshToken(user.getId());
            redisService.saveRefreshToken(user.getId(), refreshToken, Duration.ofDays(14));
            return new OAuthLoginResponseDto(true, "회원가입 완료", false, accessToken, refreshToken);

        } catch (DataIntegrityViolationException e) {
            throw new GlobalException(GlobalErrorCode.ALREADY_REGISTERED);
        }
    }

    public NicknameCheckResponseDto checkNickname(String nickname) {
        if(!nicknamePattern.matcher(nickname).matches()) {
            throw new GlobalException(GlobalErrorCode.NICKNAME_FORMAT_INVALID);
        }

        boolean isAvailable = userRepository.existsByNickname(nickname);

        return NicknameCheckResponseDto.builder()
                .success(true)
                .available(isAvailable)
                .message(!isAvailable ? "사용가능한 닉네임입니다." : "이미 사용중인 닉네임입니다.")
                .build();

    }

    public OAuthLogoutResponseDto logout(String accessToken, String refreshToken) {
        if(!jwtProvider.validateToken(accessToken)) {
            throw new GlobalException(GlobalErrorCode.INVALID_ACCESS_TOKEN);
        }

        Long userId = jwtProvider.getUserId(accessToken);

        String savedRefreshToken = redisService.getRefreshToken(userId);
        if(!refreshToken.equals(savedRefreshToken)) {
            throw new GlobalException(GlobalErrorCode.INVALID_REFRESH_TOKEN);
        }

        redisService.deleteRefreshToken(userId);

        long expiration = jwtProvider.getTokenRemainingTime(accessToken);
        redisService.blacklistAccessToken(accessToken, expiration);

        return new OAuthLogoutResponseDto(true, "로그아웃이 완료되었습니다.");
    }
}
