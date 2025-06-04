package com.stockleague.backend.openapi.client;

import com.stockleague.backend.infra.properties.OpenApiProperties;
import com.stockleague.backend.openapi.dto.response.KisYearlyPriceResponseDto;
import com.stockleague.backend.openapi.service.OpenApiService;
import com.stockleague.backend.stock.dto.response.stock.StockYearlyPriceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class KisApiClient {

    private final WebClient kisWebClient;
    private final OpenApiService openApiService;
    private final OpenApiProperties openApiProperties;

    public KisApiClient(
            @Qualifier("kisApiWebClient") WebClient kisWebClient,
            OpenApiService openApiService,
            OpenApiProperties openApiProperties
    ) {
        this.kisWebClient = kisWebClient;
        this.openApiService = openApiService;
        this.openApiProperties = openApiProperties;
    }

    private static final String TR_ID = "FHKST03010100";

    public List<StockYearlyPriceDto> getYearlyPrices(String ticker, int year) {
        String uri = "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";

        String accessToken = openApiService.getValidAccessToken().block();
        if (accessToken == null) {
            log.warn("[KIS API] AccessToken 획득 실패 - ticker: {}, year: {}", ticker, year);
            return List.of();
        }

        try {
            log.debug("[KIS API] 연봉 시세 요청 시작 - ticker: {}, year: {}", ticker, year);
            log.debug("[KIS API] 요청 URI: {}", uri);
            log.debug("[KIS API] 요청 헤더 - appkey: {}, appsecret: {}, tr_id: {}, authorization: Bearer {}",
                    openApiProperties.getAppKey(),
                    openApiProperties.getAppSecret(),
                    TR_ID,
                    accessToken.substring(0, Math.min(accessToken.length(), 10)) + "...");

            return kisWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(uri)
                            .queryParam("fid_cond_mrkt_div_code", "J")
                            .queryParam("fid_input_iscd", ticker)
                            .queryParam("fid_period_div_code", "Y")
                            .queryParam("fid_org_adj_prc", "0")
                            .build())
                    .header("authorization", "Bearer " + accessToken)
                    .header("appkey", openApiProperties.getAppKey())
                    .header("appsecret", openApiProperties.getAppSecret())
                    .header("tr_id", TR_ID)
                    .retrieve()
                    .onStatus(status -> status.isError(), response ->
                            response.bodyToMono(String.class)
                                    .doOnNext(errorBody -> log.error("[KIS API] 에러 응답 바디: {}", errorBody))
                                    .flatMap(body -> Mono.error(new RuntimeException("KIS API 오류 응답"))))
                    .bodyToMono(KisYearlyPriceResponseDto.class)
                    .map(response -> {
                        List<StockYearlyPriceDto> dtos = response.toDtoList(ticker).stream()
                                .filter(dto -> dto.year() == year)
                                .toList();
                        log.debug("[KIS API] 응답 수신 성공 - ticker: {}, year: {}, 변환된 데이터 건수: {}", ticker, year, dtos.size());
                        return dtos;
                    })
                    .doOnError(e ->
                            log.warn("[KIS API] 연봉 데이터 조회 실패 - ticker: {}, year: {}, error: {}", ticker, year,
                                    e.getMessage()))
                    .block();
        } catch (Exception e) {
            log.error("[KIS API] 연봉 데이터 조회 예외 발생 - ticker: {}, year: {}, exception: {}", ticker, year, e.getMessage(), e);
            return List.of();
        }
    }
}
