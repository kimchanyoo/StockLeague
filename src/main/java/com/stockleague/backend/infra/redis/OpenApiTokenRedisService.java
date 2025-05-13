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

    public void saveAccessToken(String accessToken, long expiresInSeconds) {
        redisTemplate.opsForValue().set(KEY, accessToken, Duration.ofSeconds(expiresInSeconds));
    }

    public String getAccessToken() {
        return redisTemplate.opsForValue().get(KEY);
    }

    public boolean hasValidAccessToken() {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY));
    }
}
