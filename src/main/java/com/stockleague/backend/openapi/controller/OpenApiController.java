package com.stockleague.backend.openapi.controller;

import com.stockleague.backend.openapi.dto.response.OpenApiTokenResponseDto;
import com.stockleague.backend.openapi.service.OpenApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/openapi")
public class OpenApiController {

    private final OpenApiService openApiService;

    @GetMapping("/token")
    public Mono<OpenApiTokenResponseDto> getToken() {
        return openApiService.requestAccessToken();
    }
}
