package com.stockleague.backend.openapi.client;

import com.stockleague.backend.infra.properties.OpenApiProperties;
import com.stockleague.backend.openapi.dto.request.OpenApiTokenRequestDto;
import com.stockleague.backend.openapi.dto.request.RealtimeKeyRequestDto;
import com.stockleague.backend.openapi.dto.response.OpenApiTokenResponseDto;
import com.stockleague.backend.openapi.dto.response.RealtimeKeyResponseDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class OpenApiClient {

    private final WebClient openApiWebClient;
    private final OpenApiProperties openApiProperties;

    public OpenApiClient(@Qualifier("openApiWebClient") WebClient openApiWebClient,
                         OpenApiProperties openApiProperties) {
        this.openApiWebClient = openApiWebClient;
        this.openApiProperties = openApiProperties;
    }

    public Mono<OpenApiTokenResponseDto> requestAccessToken() {
        OpenApiTokenRequestDto request = new OpenApiTokenRequestDto(
                openApiProperties.getGrantType(),
                openApiProperties.getAppKey(),
                openApiProperties.getAppSecret()
        );

        return openApiWebClient.post()
                .uri("/oauth2/tokenP")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenApiTokenResponseDto.class);
    }

    public Mono<RealtimeKeyResponseDto> requestRealtimeKey() {
        RealtimeKeyRequestDto request = new RealtimeKeyRequestDto(
                openApiProperties.getGrantType(),
                openApiProperties.getAppKey(),
                openApiProperties.getAppSecret()
        );

        return openApiWebClient.post()
                .uri("/oauth2/approval")
                .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RealtimeKeyResponseDto.class);
    }
}
