package com.stockleague.backend.auth.oauth.client;

import com.stockleague.backend.auth.dto.request.OAuthLoginRequestDto;
import com.stockleague.backend.auth.oauth.info.KaKaoUserInfo;
import com.stockleague.backend.auth.oauth.info.OAuthUserInfo;
import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.infra.properties.KakaoProperties;
import com.stockleague.backend.user.domain.OauthServerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
@Qualifier("kakaoOAuthClient")
public class KakaoOAuthClient implements OAuthClient {

    private final WebClient kakaoAuthWebClient;
    private final WebClient kakaoApiWebClient;
    private final KakaoProperties kakaoProperties;

    public KakaoOAuthClient(
            @Qualifier("kakaoAuthWebClient") WebClient kakaoAuthWebClient,
            @Qualifier("kakaoApiWebClient") WebClient kakaoApiWebClient,
            KakaoProperties kakaoProperties
    ) {
        this.kakaoAuthWebClient = kakaoAuthWebClient;
        this.kakaoApiWebClient = kakaoApiWebClient;
        this.kakaoProperties = kakaoProperties;
    }

    @Override
    public boolean supports(OauthServerType provider) {
        return provider == OauthServerType.KAKAO;
    }

    @Override
    public OAuthUserInfo requestUserInfo(OAuthLoginRequestDto requestDto){
        log.info("[Kakao] 인가 코드로 토큰 요청");

        try{
            // Access Token 요청
            Map<String, Object> tokenResponse = kakaoAuthWebClient.post()
                    .uri(kakaoProperties.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue("grant_type=authorization_code" +
                            "&client_id=" + kakaoProperties.getClientId() +
                            "&client_secret=" + kakaoProperties.getClientSecret() +
                            "&redirect_uri=" + kakaoProperties.getRedirectUri() +
                            "&code=" + requestDto.getAuthCode())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                throw new GlobalException(GlobalErrorCode.OAUTH_AUTH_FAILED);
            }

            String accessToken = (String) tokenResponse.get("access_token");

            Map<String, Object> userAttributes = kakaoApiWebClient.get()
                    .uri(kakaoProperties.getUserInfoUri())
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            log.info("[Kakao] 사용자 정보 조회 완료");

            return new KaKaoUserInfo(userAttributes);
        }catch (WebClientResponseException e) {
            log.warn("[KakaoOAuthClient] WebClientResponseException: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().is4xxClientError()) {
                throw new GlobalException(GlobalErrorCode.OAUTH_AUTH_FAILED);
            } else {
                throw new GlobalException(GlobalErrorCode.OAUTH_SERVER_ERROR);
            }

        } catch (IllegalArgumentException | ClassCastException e) {
            log.error("[KakaoOAuthClient] 파싱 중 오류 발생", e);
            throw new GlobalException(GlobalErrorCode.OAUTH_SERVER_ERROR);

        } catch (Exception e) {
            log.error("[KakaoOAuthClient] 예기치 못한 오류 발생", e);
            throw new GlobalException(GlobalErrorCode.OAUTH_SERVER_ERROR);
        }



    }


}
