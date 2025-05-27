package com.stockleague.backend.auth.service;

import com.stockleague.backend.auth.dto.request.AdditionalInfoRequestDto;
import com.stockleague.backend.auth.dto.request.OAuthLoginRequestDto;
import com.stockleague.backend.auth.dto.response.OAuthLoginResponseDto;
import com.stockleague.backend.auth.dto.response.OAuthLogoutResponseDto;
import com.stockleague.backend.auth.dto.response.TokenReissueResponseDto;
import com.stockleague.backend.auth.jwt.JwtProvider;
import com.stockleague.backend.auth.jwt.JwtProvider.OauthTokenPayload;
import com.stockleague.backend.auth.oauth.client.OAuthClient;
import com.stockleague.backend.auth.oauth.info.OAuthUserInfo;
import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.global.handler.TokenCookieHandler;
import com.stockleague.backend.global.validator.RedirectUriValidator;
import com.stockleague.backend.infra.redis.TokenRedisService;
import com.stockleague.backend.user.domain.OauthServerType;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.domain.UserRole;
import com.stockleague.backend.user.dto.response.NicknameCheckResponseDto;
import com.stockleague.backend.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final List<OAuthClient> oauthClients;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final TokenRedisService redisService;
    private static final Pattern nicknamePattern = Pattern.compile("^[a-zA-Z0-9가-힣]{2,10}$");
    private final TokenCookieHandler tokenCookieHandler;

    public OAuthLoginResponseDto login(OAuthLoginRequestDto requestDto, HttpServletResponse response) {
        OAuthClient client = oauthClients.stream()
                .filter(c -> c.supports(OauthServerType.valueOf(requestDto.provider())))
                .findFirst()
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.UNSUPPORTED_OAUTH_PROVIDER));

        if(!RedirectUriValidator.isAllowed(requestDto.redirectUri())) {
            log.warn("허용되지 않은 redirectUri 요청: {}", requestDto.redirectUri());
            throw new GlobalException(GlobalErrorCode.INVALID_REDIRECT_URI);
        }

        OAuthUserInfo userInfo = client.requestUserInfo(requestDto);

        return userRepository.findByOauthIdAndProvider(userInfo.getOauthId(), userInfo.getProvider())
                .map(user -> {
                    if (Boolean.TRUE.equals(user.getIsBanned())) {
                        throw new GlobalException(GlobalErrorCode.BANNED_USER);
                    }

                    issueTokensAndSetCookies(user, response);
                    String role = user.getRole().toString();
                    String nickname = user.getNickname();

                    return new OAuthLoginResponseDto(true, "소셜 로그인 성공", false,
                            null, nickname, role);
                })
                .orElseGet(() -> {
                    String tempAccessToken = jwtProvider.createTempAccessToken(userInfo.getOauthId(),
                            userInfo.getProvider());
                    return new OAuthLoginResponseDto(true, "추가 정보 입력 필요", true,
                            tempAccessToken, null, null);
                });
    }

    public OAuthLoginResponseDto completeSignup(String tempToken, AdditionalInfoRequestDto requestDto,
                                                HttpServletResponse response) {

        OauthTokenPayload payload = jwtProvider.parseTempToken(tempToken);
        String oauthId = payload.oauthId();
        var provider = payload.provider();


        if (userRepository.findByOauthIdAndProvider(oauthId, provider).isPresent()) {
            throw new GlobalException(GlobalErrorCode.ALREADY_REGISTERED);
        }

        if (!Boolean.TRUE.equals(requestDto.isOverFifteen())) {
            throw new GlobalException(GlobalErrorCode.AGE_RESTRICTION);
        }

        try {
            User user = userRepository.save(User.builder()
                    .oauthId(oauthId)
                    .provider(provider)
                    .nickname(requestDto.nickname())
                    .role(UserRole.USER)
                    .agreedToTerms(requestDto.agreedToTerms())
                    .isOverFifteen(requestDto.isOverFifteen())
                    .build()
            );

            issueTokensAndSetCookies(user, response);

            String role = user.getRole().toString();
            String nickname = user.getNickname();

            return new OAuthLoginResponseDto(true, "추가 정보 입력이 완료되었습니다",
                    false, null, nickname, role);

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
                .available(!isAvailable)
                .message(!isAvailable ? "사용가능한 닉네임입니다." : "이미 사용중인 닉네임입니다.")
                .build();

    }

    public OAuthLogoutResponseDto logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = jwtProvider.resolveToken(request);
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (!jwtProvider.validateToken(accessToken)) {
            throw new GlobalException(GlobalErrorCode.INVALID_ACCESS_TOKEN);
        }

        Long userId = jwtProvider.getUserId(accessToken);
        if (!redisService.isRefreshTokenValid(userId, refreshToken)) {
            tokenCookieHandler.removeTokenCookies(response);
            throw new GlobalException(GlobalErrorCode.INVALID_REFRESH_TOKEN);
        }

        redisService.deleteRefreshToken(userId);
        long expiration = jwtProvider.getTokenRemainingTime(accessToken);
        redisService.blacklistAccessToken(accessToken, expiration);

        tokenCookieHandler.removeTokenCookies(response);

        return new OAuthLogoutResponseDto(true, "로그아웃이 완료되었습니다.");
    }

    public TokenReissueResponseDto reissueToken(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = extractRefreshTokenFromCookie(request);
        Long userId = getValidUserIdFromRefreshToken(refreshToken, response);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsBanned())) {
            throw new GlobalException(GlobalErrorCode.BANNED_USER);
        }

        String newAccessToken = jwtProvider.createAccessToken(userId);
        String newRefreshToken = jwtProvider.createRefreshToken(userId);

        redisService.rotateRefreshToken(userId, newRefreshToken, Duration.ofDays(30));
        tokenCookieHandler.addTokenCookies(response, newAccessToken, newRefreshToken);

        return new TokenReissueResponseDto(true, "토큰이 재발급되었습니다.");
    }

    public void clearUserTokens(Long userId, HttpServletRequest request, HttpServletResponse response) {
        String accessToken = jwtProvider.resolveToken(request);

        if (accessToken == null) {
            log.warn("[탈퇴 처리] accessToken 없음 - userId: {}", userId);
        } else {
            try {
                long remaining = jwtProvider.getTokenRemainingTime(accessToken);
                redisService.blacklistAccessToken(accessToken, remaining);
            } catch (Exception e) {
                redisService.blacklistAccessToken(accessToken, 1000L); // 최소 TTL
                log.warn("[탈퇴 처리] accessToken 파싱 실패 - userId: {}, token: {}", userId, accessToken);
            }
        }

        redisService.deleteRefreshToken(userId);
        tokenCookieHandler.removeTokenCookies(response);
    }

    private void issueTokensAndSetCookies(User user, HttpServletResponse response) {
        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        redisService.saveRefreshToken(user.getId(), refreshToken, Duration.ofDays(30));
        tokenCookieHandler.addTokenCookies(response, accessToken, refreshToken);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        return Arrays.stream(request.getCookies())
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.INVALID_REFRESH_TOKEN));
    }

    private Long getValidUserIdFromRefreshToken(String refreshToken, HttpServletResponse response) {
        if (!jwtProvider.validateToken(refreshToken)) {
            tokenCookieHandler.removeTokenCookies(response);
            throw new GlobalException(GlobalErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtProvider.getUserId(refreshToken);
        if (!redisService.isRefreshTokenValid(userId, refreshToken)) {
            tokenCookieHandler.removeTokenCookies(response);
            throw new GlobalException(GlobalErrorCode.INVALID_REFRESH_TOKEN);
        }

        return userId;
    }
}
