package com.stockleague.backend.openapi.service;

import com.stockleague.backend.infra.redis.OpenApiTokenRedisService;
import com.stockleague.backend.openapi.client.OpenApiClient;
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
}
