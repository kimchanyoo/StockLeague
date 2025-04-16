package com.stockleague.backend.infra.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenRedisService {

    private final StringRedisTemplate redisTemplate;

    public void saveRefreshToken(Long userId, String refreshToken, Duration expiration) {
        redisTemplate.opsForValue().set("refresh:" + userId, refreshToken, expiration);
    }

    public String getRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get("refresh:" + userId);
    }

    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete("refresh:" + userId);
    }

    public void blacklistAccessToken(String token, long expiration) {
        redisTemplate.opsForValue().set("BL:" + token, "logout", Duration.ofMillis(expiration));
    }

    public boolean isBlacklisted(String accessToken) {
        return redisTemplate.hasKey("BL:" + accessToken);
    }
}
