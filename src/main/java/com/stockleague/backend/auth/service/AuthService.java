package com.stockleague.backend.auth.service;

import com.stockleague.backend.auth.dto.request.AdditionalInfoRequestDto;
import com.stockleague.backend.auth.dto.response.OAuthLoginResponseDto;
import com.stockleague.backend.auth.jwt.JwtProvider;
import com.stockleague.backend.auth.jwt.JwtProvider.OauthTokenPayload;
import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.infra.redis.TokenRedisService;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.domain.UserRole;
import com.stockleague.backend.user.repository.UserRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final TokenRedisService redisService;

    public OAuthLoginResponseDto completeSignup(String tempToken, AdditionalInfoRequestDto requestDto) {

        OauthTokenPayload payload = jwtProvider.parseTempToken(tempToken);
        String oauthId = payload.oauthId();
        var provider = payload.provider();

        if (userRepository.findByOauthIdAndProvider(oauthId, provider).isPresent()) {
            throw new GlobalException(GlobalErrorCode.ALREADY_REGISTERED);
        }

        try {

            System.out.println("==== [회원가입 진입] ====");
            System.out.println("oauthId: " + oauthId);
            System.out.println("provider: " + provider);
            System.out.println("nickname: " + requestDto.getNickname());
            System.out.println("agreedToTerms: " + requestDto.getAgreedToTerms());
            System.out.println("isOverFifteen: " + requestDto.getIsOverFifteen());

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
            String refreshToken = jwtProvider.createRefreshToken();
            redisService.saveRefreshToken(user.getId(), refreshToken, Duration.ofDays(14));
            return new OAuthLoginResponseDto(true, "회원가입 완료", false, accessToken, refreshToken);

        } catch (DataIntegrityViolationException e) {
            throw new GlobalException(GlobalErrorCode.ALREADY_REGISTERED);
        }
    }
}
