package com.stockleague.backend.openapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public Mono<String> generate(String jsonBody) {
        log.info("hashKey 요청 시작");

        return openApiClient.requestHashKey(jsonBody)
                .map(HashKeyResponseDto::hash)
                .doOnNext(hash -> log.info("hashKey 생성 완료: {}", hash))
                .doOnError(e -> log.error("hashKey 생성 실패", e));
    }


    public Mono<String> generate(Object body) {
        try {
            String json = objectMapper.writeValueAsString(body);
            return generate(json);
        } catch (JsonProcessingException e) {
            log.error("요청 객체 직렬화 실패", e);
            return Mono.error(new IllegalArgumentException("요청 객체 직렬화 실패", e));
        }
    }
}
