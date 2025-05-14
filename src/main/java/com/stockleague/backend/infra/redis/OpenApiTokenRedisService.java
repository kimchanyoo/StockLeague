package com.stockleague.backend.infra.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenApiTokenRedisService {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY = "openapi:access_token";
    private static final String REALTIME_KEY = "openapi:realtime_key";

    public void saveAccessToken(String accessToken, long expiresInSeconds) {
        redisTemplate.opsForValue().set(KEY, accessToken, Duration.ofSeconds(expiresInSeconds));
    }

    public String getAccessToken() {
        return redisTemplate.opsForValue().get(KEY);
    }

    public boolean hasValidAccessToken() {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY));
    }

    public void saveRealTimeKey(String approvalKey, long ttlSeconds) {
        redisTemplate.opsForValue().set(REALTIME_KEY, approvalKey, Duration.ofSeconds(ttlSeconds));
    }

    public String getRealTimeKey() {
        return redisTemplate.opsForValue().get(REALTIME_KEY);
    }

    public boolean hasRealTimeKey() {
        return Boolean.TRUE.equals(redisTemplate.hasKey(REALTIME_KEY));
    }

    public void deleteRealTimeKey() {
        redisTemplate.delete(REALTIME_KEY);
    }
}
