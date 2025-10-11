package com.stockleague.backend.openapi.initializer;

import com.stockleague.backend.openapi.service.OpenApiService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessTokenInitializer {

    private final OpenApiService openApiService;

    @PostConstruct
    public void initAccessToken() {
        openApiService.getValidAccessToken()
                .doOnNext(token -> log.info("서버 기동 시 access_token 초기화 완료"))
                .doOnError(e -> log.error("서버 기동 시 access_token 초기화 실패", e))
                .subscribe();
    }
}
