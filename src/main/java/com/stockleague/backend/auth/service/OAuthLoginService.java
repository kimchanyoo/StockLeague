package com.stockleague.backend.auth.service;

import com.stockleague.backend.auth.dto.request.OAuthLoginRequestDto;
import com.stockleague.backend.auth.dto.response.OAuthLoginResponseDto;
import com.stockleague.backend.auth.jwt.JwtProvider;
import com.stockleague.backend.auth.oauth.client.OAuthClient;
import com.stockleague.backend.auth.oauth.info.OAuthUserInfo;
import com.stockleague.backend.infra.redis.TokenRedisService;
import com.stockleague.backend.user.domain.OauthServerType;
import com.stockleague.backend.user.repository.UserRepository;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

    private final List<OAuthClient> oauthClients;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final TokenRedisService redisService;

    public OAuthLoginResponseDto login(OAuthLoginRequestDto requestDto) {
        OAuthClient client = oauthClients.stream()
                .filter(c -> c.supports(OauthServerType.valueOf(requestDto.getProvider())))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 소셜 로그인입니다."));

        OAuthUserInfo userInfo = client.requestUserInfo(requestDto);

        return userRepository.findByOauthIdAndProvider(userInfo.getOauthId(), userInfo.getProvider())
                .map(user -> {
                    // 기존 유저 → 바로 로그인 처리
                    String accessToken = jwtProvider.createAccessToken(user.getId());
                    System.out.println("accessToken 생성 완료");
                    String refreshToken = jwtProvider.createRefreshToken();
                    System.out.println("refreshToken 생성 완료");
                    redisService.saveRefreshToken(user.getId(), refreshToken, Duration.ofDays(14));
                    System.out.println("Redis 저장 완료");

                    return new OAuthLoginResponseDto(true, "소셜 로그인 성공", false,
                            accessToken, refreshToken);
                })
                .orElseGet(() -> {
                    // 신규 유저 → accessToken 없이 isFirstLogin true 반환
                    String tempAccessToken = jwtProvider.createTempAccessToken(userInfo.getOauthId(),
                            userInfo.getProvider());
                    return new OAuthLoginResponseDto(true, "추가 정보 입력 필요", true,
                            tempAccessToken, null);
                });
    }
}
