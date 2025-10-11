package com.stockleague.backend.infra.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenRedisService {

    private final StringRedisTemplate redisTemplate;

    private static final String REFRESH_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "BL:";

    public void saveRefreshToken(Long userId, String refreshToken, Duration expiration) {
        String key = REFRESH_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, expiration);
    }

    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(REFRESH_PREFIX + userId);
    }

    public boolean isRefreshTokenValid(Long userId, String token) {
        String saved = getRefreshToken(userId);
        return saved != null && saved.equals(token);
    }

    public void rotateRefreshToken(Long userId, String newToken, Duration expiration) {
        deleteRefreshToken(userId);
        saveRefreshToken(userId, newToken, expiration);
    }

    public void blacklistAccessToken(String token, long expirationMillis) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token, "logout", Duration.ofMillis(expirationMillis));
    }

    public boolean isBlacklisted(String token) {
        Boolean result = redisTemplate.hasKey(BLACKLIST_PREFIX + token);
        return Boolean.TRUE.equals(result);
    }

    private String getRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get(REFRESH_PREFIX + userId);
    }
}
