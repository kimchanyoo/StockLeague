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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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

    // JWT 서명용 키 생성
    private Key getSigningKey() {
        try{
            byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
            return Keys.hmacShaKeyFor(keyBytes);
        }catch(Exception e){
            log.error("JWT key decode error: {}", e.getMessage());
            throw new IllegalStateException("JWT 비밀키 설정이 잘못되었습니다.");
        }
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
                .setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15분 유효
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("토큰 유효성 검사 실패: {}", e.getMessage());
            return false;
        }
    }

    // 만료 시간 구하는 메서드
    public long getTokenRemainingTime(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    // userId 추출
    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    // 인증 객체 생성
    public Authentication getAuthentication(String token) {
        Long userId = getUserId(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        String role = user.getRole().name(); // USER or ADMIN

        return new UsernamePasswordAuthenticationToken(
                userId.toString(),
                null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    // HTTP 요청에서 JWT 추출
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        return null;
    }

    // JWT 파싱
    protected Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            log.error("JWT 파싱 실패: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            throw new GlobalException(GlobalErrorCode.INVALID_TEMP_TOKEN);
        }
    }

    // 추가 정보 입력용 토큰 분석기
    public OauthTokenPayload parseTempToken(String token) {
        try {
            Claims claims = parseClaims(token);

            // type 필드 확인
            Object typeObj = claims.get("type");
            if (!(typeObj instanceof String type) || !"temp".equals(type)) {
                throw new GlobalException(GlobalErrorCode.INVALID_TEMP_TOKEN);
            }

            // provider 필드 확인
            Object providerObj = claims.get("provider");
            if (!(providerObj instanceof String providerStr)) {
                throw new GlobalException(GlobalErrorCode.INVALID_TEMP_TOKEN);
            }

            String oauthId = claims.getSubject();
            OauthServerType provider = OauthServerType.valueOf(providerStr);

            return new OauthTokenPayload(oauthId, provider);

        } catch (IllegalArgumentException | NullPointerException e) {
            log.error("Claim 파싱 오류: {}", e.getMessage());
            throw new GlobalException(GlobalErrorCode.INVALID_TEMP_TOKEN);
        } catch (Exception e) {
            log.error("기타 예외 발생: {}", e.getMessage());
            throw new GlobalException(GlobalErrorCode.INVALID_TEMP_TOKEN);
        }
    }

    // 내부 정적 클래스
    public record OauthTokenPayload(String oauthId, OauthServerType provider) {}
}
