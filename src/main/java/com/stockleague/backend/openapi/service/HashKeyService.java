package com.stockleague.backend.openapi.service;

import com.stockleague.backend.openapi.client.OpenApiClient;
import com.stockleague.backend.openapi.dto.response.HashKeyResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class HashKeyService {

    private final OpenApiClient openApiClient;

    public Mono<String> generate(Object requestData) {
        log.info("HashKey 요청 시작: {}", requestData);

        return openApiClient.requestHashKey(requestData)
                .map(HashKeyResponseDto::hash)
                .doOnNext(hash -> log.info("HashKey 생성 완료: {}", hash))
                .doOnError(e -> log.error("HashKey 생성 실패", e));
    }
}
