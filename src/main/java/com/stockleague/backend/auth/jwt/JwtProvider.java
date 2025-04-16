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
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;

    @PostConstruct
    public void init() {
        String secret = jwtProperties.getSecret();
        System.out.println("[JWT Secret 설정 확인]");
        System.out.println("값: " + secret);
        System.out.println("길이: " + (secret != null ? secret.length() : "null"));

        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            System.out.println("Base64 디코딩 성공: " + keyBytes.length + " bytes");
        } catch (Exception e) {
            System.out.println("Base64 디코딩 실패: " + e.getMessage());
        }
    }

    // JWT 서명용 키 생성
    private Key getSigningKey() {
        try{
            byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
            return Keys.hmacShaKeyFor(keyBytes);
        }catch(Exception e){
            System.out.println("JWT Secret 디코딩 에러: " + e.getMessage());
            return null;
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
    public String createRefreshToken() {
        return Jwts.builder()
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenValidity()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 임시 access토큰 생성
    public String createTempAccessToken(String oauthId, OauthServerType provider) {
        return Jwts.builder()
                .setSubject(oauthId)
                .claim("type", "temp")
                .claim("provider", provider.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 5 * 60 * 1000)) // 5분 유효
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
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
                userId,
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
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 추가 정보 입력용 토큰 분석기
    public OauthTokenPayload parseTempToken(String token) {
        try{
            Claims claims = parseClaims(token);

            String type = (String) claims.get("type");
            if (!"temp".equals(type)) {
                throw new GlobalException(GlobalErrorCode.INVALID_TEMP_TOKEN);
            }

            String oauthId = claims.getSubject();
            OauthServerType provider = OauthServerType.valueOf((String) claims.get("provider"));

            return new OauthTokenPayload(oauthId, provider);
        }catch (Exception e){
            throw new GlobalException(GlobalErrorCode.INVALID_TEMP_TOKEN);
        }
    }

    // 내부 정적 클래스 (혹은 별도 파일)
    public record OauthTokenPayload(String oauthId, OauthServerType provider) {}
}
