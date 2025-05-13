package com.stockleague.backend.openapi.client;

import com.stockleague.backend.infra.properties.OpenApiProperties;
import com.stockleague.backend.openapi.dto.request.OpenApiTokenRequestDto;
import com.stockleague.backend.openapi.dto.request.RealtimeKeyRequestDto;
import com.stockleague.backend.openapi.dto.response.OpenApiTokenResponseDto;
import com.stockleague.backend.openapi.dto.response.RealtimeKeyResponseDto;
import org.springframework.beans.factory.annotation.Qualifier;
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

    public Mono<RealtimeKeyResponseDto> requestRealtimeKey(String accessToken) {
        RealtimeKeyRequestDto request = new RealtimeKeyRequestDto(
                "client_credentials",
                openApiProperties.getAppKey(),
                openApiProperties.getAppSecret()
        );

        return openApiWebClient.post()
                .uri("/oauth2/approval")
                .header("authorization", "Bearer " + accessToken)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RealtimeKeyResponseDto.class);
    }
}
