package com.stockleague.backend.global.config;

import com.stockleague.backend.infra.properties.OpenApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class OpenApiWebClientConfig {

    private final OpenApiProperties openApiProperties;

    @Bean(name = "openApiWebClient")
    public WebClient openApiWebClient() {
        return WebClient.builder()
                .baseUrl(openApiProperties.getAuthUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
