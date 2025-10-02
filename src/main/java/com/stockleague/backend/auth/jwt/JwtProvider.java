package com.stockleague.backend.auth.jwt;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.infra.properties.JwtProperties;
import com.stockleague.backend.user.domain.OauthServerType;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;

    // 서명 키 캐시
    private volatile Key cachedKey;

    private Key getSigningKey() {
        Key k = cachedKey;
        if (k == null) {
            synchronized (this) {
                k = cachedKey;
                if (k == null) {
                    try {
                        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
                        cachedKey = k = Keys.hmacShaKeyFor(keyBytes);
                    } catch (Exception e) {
                        log.error("JWT key decode error: {}", e.getMessage());
                        throw new IllegalStateException("JWT 비밀키 설정이 잘못되었습니다.");
                    }
                }
            }
        }
        return k;
    }

    // accessToken 생성 (userId 기반)
    public String createAccessToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenValidity()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // refreshToken 생성
    public String createRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenValidity()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 임시 accessToken 생성
    public String createTempAccessToken(String oauthId, OauthServerType provider) {
        return Jwts.builder()
                .setSubject(oauthId)
                .claim("type", "temp")
                .claim("provider", provider.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15분
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 유효성 검사 (true/false만)
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("토큰 유효성 검사 실패: {}", e.getMessage());
            return false;
        }
    }

    // 만료 시간(ms)
    public long getTokenRemainingTime(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    // userId 추출
    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    // 인증 객체 생성 (access 전용)
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        String type = claims.get("type", String.class);
        if (!"access".equals(type)) {
            throw new GlobalException(GlobalErrorCode.INVALID_ACCESS_TOKEN);
        }

        Long userId = Long.valueOf(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        String role = "ROLE_" + user.getRole().name(); // USER/ADMIN

        return new UsernamePasswordAuthenticationToken(
                userId,
                null,
                Collections.singleton(new SimpleGrantedAuthority(role))
        );
    }

    // 헤더에서 JWT 추출
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    // JWT 파싱 (예외 래핑 금지, 시계 오차 허용)
    protected Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .setAllowedClockSkewSeconds(30) // 옵션
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 추가 정보 입력용 temp 토큰 파싱
    public OauthTokenPayload parseTempToken(String token) {
        try {
            Claims claims = parseClaims(token);

            String type = claims.get("type", String.class);
            if (!"temp".equals(type)) {
                throw new GlobalException(GlobalErrorCode.INVALID_TEMP_TOKEN);
            }

            String providerStr = claims.get("provider", String.class);
            if (providerStr == null) {
                throw new GlobalException(GlobalErrorCode.INVALID_TEMP_TOKEN);
            }

            String oauthId = claims.getSubject();
            OauthServerType provider = OauthServerType.valueOf(providerStr);

            return new OauthTokenPayload(oauthId, provider);

        } catch (IllegalArgumentException | NullPointerException | JwtException e) {
            log.error("Temp 토큰 파싱 실패: {}", e.getMessage());
            throw new GlobalException(GlobalErrorCode.INVALID_TEMP_TOKEN);
        } catch (Exception e) {
            log.error("Temp 토큰 처리 중 예외: {}", e.getMessage());
            throw new GlobalException(GlobalErrorCode.INVALID_TEMP_TOKEN);
        }
    }

    // 테스트 유저용 accessToken 생성
    public String createTestAccessToken(Long userId, int years) {
        Date exp = Date.from(OffsetDateTime.now(ZoneOffset.UTC).plusYears(years).toInstant());
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("type", "access")
                .claim("test", true)
                .setIssuedAt(new Date())
                .setExpiration(exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 내부 정적 클래스
    public record OauthTokenPayload(String oauthId, OauthServerType provider) {}
}
