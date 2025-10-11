package com.stockleague.backend.global.config;

import com.stockleague.backend.infra.properties.KakaoProperties;
import com.stockleague.backend.infra.properties.OpenApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final KakaoProperties kakaoPropertise;
    private final OpenApiProperties openApiProperties;

    // 인가코드 -> 액세스 토큰 요청용
    @Bean(name = "kakaoAuthWebClient")
    public WebClient kakaoWebClient() {
        return WebClient.builder()
                .baseUrl(kakaoPropertise.getAuthBaseUrl())
                .build();
    }

    // AccessToken -> 사용자 정보 요청용
    @Bean(name = "kakaoApiWebClient")
    public WebClient kakaoApiWebClient() {
        return WebClient.builder()
                .baseUrl(kakaoPropertise.getApiBaseUrl())
                .build();
    }

    @Bean(name = "kisApiWebClient")
    public WebClient kisWebClient() {
        return WebClient.builder()
                .baseUrl(openApiProperties.getAuthUrl())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
