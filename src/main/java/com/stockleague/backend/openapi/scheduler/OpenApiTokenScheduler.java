package com.stockleague.backend.openapi.scheduler;

import com.stockleague.backend.infra.redis.OpenApiTokenRedisService;
import com.stockleague.backend.openapi.client.OpenApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenApiTokenScheduler {

    private final OpenApiClient openApiClient;
    private final OpenApiTokenRedisService redisService;

    @Scheduled(fixedRate = 1000 * 60 * 60 * 23)
    public void refreshAccessToken() {
        log.info("OpenAPI access_token 갱신 작업 시작");

        openApiClient.requestAccessToken()
                .doOnNext(token -> {
                    long expiresIn = token.expiresIn();

                    redisService.saveAccessToken(token.accessToken(), expiresIn);
                    log.info("access_token 갱신 완료 (TTL: {}초)", expiresIn);
                })
                .doOnError(e -> log.error("access_token 갱신 실패", e))
                .subscribe();
    }
}
