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

    private static final String TR_ID = "FHKST03010400";

    public List<StockYearlyPriceDto> getYearlyPrices(String ticker, int year) {
        String uri = "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";

        String accessToken = openApiService.getValidAccessToken().block();
        if (accessToken == null) {
            log.warn("[KIS API] AccessToken 획득 실패 - ticker: {}, year: {}", ticker, year);
            return List.of();
        }

        try {
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
                    .bodyToMono(KisYearlyPriceResponseDto.class)
                    .map(response -> response.toDtoList(ticker).stream()
                            .filter(dto -> dto.year() == year)
                            .toList())
                    .doOnError(e ->
                            log.warn("[KIS API] 연봉 데이터 조회 실패 - ticker: {}, year: {}, error: {}", ticker, year,
                            e.getMessage()))
                    .block();
        } catch (Exception e) {
            log.warn("[KIS API] 연봉 데이터 조회 예외 발생 - ticker: {}, year: {}, exception: {}", ticker, year, e.getMessage());
            return List.of();
        }
    }
}
