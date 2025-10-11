package com.stockleague.backend.openapi.initializer;

import com.stockleague.backend.openapi.service.OpenApiService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeKeyInitializer {

    private final OpenApiService openApiService;

    @PostConstruct
    public void initRealtimeKey() {
        openApiService.getOrCreateApprovalKey()
                .doOnNext(key -> log.info("서버 시작 시 실시간 접속키 초기화 완료"))
                .doOnError(e -> log.error("서버 시작 시 실시간 접속키 초기화 실패", e))
                .subscribe();
    }
}
