package com.stockleague.backend.global.config;

import com.stockleague.backend.infra.properties.KakaoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final KakaoProperties kakaoPropertise;

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
}
