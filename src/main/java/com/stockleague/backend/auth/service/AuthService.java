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
import com.stockleague.backend.user.domain.UserAsset;
import com.stockleague.backend.user.domain.UserRole;
import com.stockleague.backend.user.dto.response.NicknameCheckResponseDto;
import com.stockleague.backend.user.repository.UserAssetRepository;
import com.stockleague.backend.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
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
    private final UserAssetRepository userAssetRepository;
    private final TokenRedisService redisService;
    private static final Pattern nicknamePattern = Pattern.compile("^[a-zA-Z0-9가-힣]{2,10}$");
    private final TokenCookieHandler tokenCookieHandler;

    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    public OAuthLoginResponseDto login(OAuthLoginRequestDto requestDto,
                                       HttpServletResponse response,
                                       HttpServletRequest httpRequest) {
        OAuthClient client = oauthClients.stream()
                .filter(c -> c.supports(OauthServerType.valueOf(requestDto.provider())))
                .findFirst()
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.UNSUPPORTED_OAUTH_PROVIDER));

        String host = httpRequest.getHeader("X-Forwarded-Host");
        String proto = httpRequest.getHeader("X-Forwarded-Proto");

        if (host == null) {
            host = httpRequest.getHeader("Host");
        }
        if (proto == null) {
            proto = httpRequest.getScheme();
        }

        String effectiveRedirectUri = proto + "://" + host + "/auth/callback";

        if(!RedirectUriValidator.isAllowed(effectiveRedirectUri)) {
            log.warn("허용되지 않은 redirectUri 요청: {}", effectiveRedirectUri);
            throw new GlobalException(GlobalErrorCode.INVALID_REDIRECT_URI);
        }

        OAuthUserInfo userInfo = client.requestUserInfo(requestDto);

        return userRepository.findByOauthIdAndProvider(userInfo.getOauthId(), userInfo.getProvider())
                .map(user -> {
                    if (Boolean.TRUE.equals(user.getIsBanned())) {
                        throw new GlobalException(GlobalErrorCode.BANNED_USER);
                    }

                    String accessToken = issueTokens(user, response);

                    String role = user.getRole().toString();
                    String nickname = user.getNickname();

                    return new OAuthLoginResponseDto(true, "소셜 로그인 성공", false,
                            accessToken, nickname, role);
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

            UserAsset userAsset = UserAsset.builder()
                    .user(user)
                    .userId(user.getId())
                    .cashBalance(BigDecimal.valueOf(10_000_000L))
                    .updatedAt(LocalDateTime.now())
                    .build();

            UserAsset savedUserAsset = userAssetRepository.save(userAsset);

            user.setUserAsset(savedUserAsset);

            String accessToken = issueTokens(user, response);

            String role = user.getRole().toString();
            String nickname = user.getNickname();

            return new OAuthLoginResponseDto(true, "추가 정보 입력이 완료되었습니다",
                    false, accessToken, nickname, role);

        } catch (DataIntegrityViolationException e) {
            throw new GlobalException(GlobalErrorCode.DATABASE_CONSTRAINT_VIOLATION);
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

        // access 토큰 null/NPE 방지 + 유효성 체크
        if (accessToken == null || !jwtProvider.validateToken(accessToken)) {
            // access가 아예 없거나 잘못된 경우에도, 클라이언트 쿠키 정리 후 "성공"으로 반환 (멱등 로그아웃)
            tokenCookieHandler.removeRefreshTokenCookie(response);
            return new OAuthLogoutResponseDto(true, "로그아웃이 완료되었습니다.");
        }

        Long userId = jwtProvider.getUserId(accessToken);

        // refresh 쿠키가 있으면만 서버측 무효화 시도
        findRefreshTokenInCookies(request).ifPresentOrElse(refreshToken -> {
            if (redisService.isRefreshTokenValid(userId, refreshToken)) {
                redisService.deleteRefreshToken(userId);
            } else {
                // 유효하지 않으면 서버 상태만 정리 (예외 던지지 않음)
                log.info("[logout] refresh 토큰이 유효하지 않음(이미 무효화되었거나 만료). userId={}", userId);
            }
        }, () -> {
            log.info("[logout] 요청에 refresh 쿠키 없음. userId={}", userId);
        });

        // access 블랙리스트 등록 (잔여 만료시간 TTL로)
        long expiration = 0L;
        try {
            expiration = jwtProvider.getTokenRemainingTime(accessToken);
        } catch (Exception e) {
            log.warn("[logout] access 토큰 잔여시간 계산 실패. 최소 TTL로 블랙리스트 등록");
            expiration = 1000L; // 최소 TTL
        }
        redisService.blacklistAccessToken(accessToken, expiration);

        // 클라 쿠키 제거 (항상)
        tokenCookieHandler.removeRefreshTokenCookie(response);

        return new OAuthLogoutResponseDto(true, "로그아웃이 완료되었습니다.");
    }

    public TokenReissueResponseDto reissueToken(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키가 없으면 Optional.empty → orElseThrow 로 명확한 예외
        String refreshToken = findRefreshTokenInCookies(request)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.INVALID_REFRESH_TOKEN));

        Long userId = getValidUserIdFromRefreshToken(refreshToken, response);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(user.getIsBanned())) {
            throw new GlobalException(GlobalErrorCode.BANNED_USER);
        }

        String newAccessToken = jwtProvider.createAccessToken(userId);
        String newRefreshToken = jwtProvider.createRefreshToken(userId);

        redisService.rotateRefreshToken(userId, newRefreshToken, Duration.ofDays(30));
        tokenCookieHandler.addRefreshTokenCookie(response, newRefreshToken);

        return new TokenReissueResponseDto(true, "토큰이 재발급되었습니다.", newAccessToken);
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
        tokenCookieHandler.removeRefreshTokenCookie(response);
    }

    private String issueTokens(User user, HttpServletResponse response) {
        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        redisService.saveRefreshToken(user.getId(), refreshToken, Duration.ofDays(30));
        tokenCookieHandler.addRefreshTokenCookie(response, refreshToken);

        return accessToken;
    }

    private Long getValidUserIdFromRefreshToken(String refreshToken, HttpServletResponse response) {
        if (!jwtProvider.validateToken(refreshToken)) {
            tokenCookieHandler.removeRefreshTokenCookie(response);
            throw new GlobalException(GlobalErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtProvider.getUserId(refreshToken);
        if (!redisService.isRefreshTokenValid(userId, refreshToken)) {
            tokenCookieHandler.removeRefreshTokenCookie(response);
            throw new GlobalException(GlobalErrorCode.INVALID_REFRESH_TOKEN);
        }

        return userId;
    }

    private java.util.Optional<String> findRefreshTokenInCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) return java.util.Optional.empty();
        for (Cookie c : cookies) {
            if (REFRESH_COOKIE_NAME.equals(c.getName())) {
                String v = c.getValue();
                if (v != null && !v.isBlank()) return java.util.Optional.of(v);
            }
        }
        return java.util.Optional.empty();
    }
}
