package com.stockleague.backend.openapi.service;

import com.stockleague.backend.infra.redis.OpenApiTokenRedisService;
import com.stockleague.backend.openapi.cache.RealtimeKeyCache;
import com.stockleague.backend.openapi.client.OpenApiClient;
import com.stockleague.backend.openapi.dto.response.RealtimeKeyResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenApiService {

    private final OpenApiTokenRedisService redisService;
    private final OpenApiClient openApiClient;
    private final RealtimeKeyCache realtimeKeyCache;

    public Mono<String> getValidAccessToken() {
        if (redisService.hasValidAccessToken()) {
            String cachedToken = redisService.getAccessToken();
            return Mono.just(cachedToken);
        }

        return openApiClient.requestAccessToken()
                .map(tokenResponse -> {
                    redisService.saveAccessToken(
                            tokenResponse.accessToken(),
                            tokenResponse.expiresIn()
                    );
                    return tokenResponse.accessToken();
                });
    }

    public Mono<String> getOrCreateApprovalKey() {
        if (realtimeKeyCache.hasKey()) {
            return Mono.just(realtimeKeyCache.get());
        }

        if (redisService.hasRealTimeKey()) {
            String key = redisService.getRealTimeKey();
            realtimeKeyCache.set(key);  // 메모리에도 올려줌
            return Mono.just(key);
        }

        return openApiClient.requestRealtimeKey()
                .map((RealtimeKeyResponseDto response) -> {
                    String key = response.approvalKey();
                    long ttl = response.expiresIn();
                    redisService.saveRealTimeKey(key, ttl);
                    realtimeKeyCache.set(key);
                    log.info("실시간 접속키 발급 완료: {}", key);
                    return key;
                });
    }
}
