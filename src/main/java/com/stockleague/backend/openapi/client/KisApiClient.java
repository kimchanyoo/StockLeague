package com.stockleague.backend.openapi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockleague.backend.infra.properties.OpenApiProperties;
import com.stockleague.backend.openapi.dto.response.KisMonthlyPriceResponseDto;
import com.stockleague.backend.openapi.dto.response.KisYearlyPriceResponseDto;
import com.stockleague.backend.openapi.service.OpenApiService;
import com.stockleague.backend.stock.dto.response.stock.StockMonthlyPriceDto;
import com.stockleague.backend.stock.dto.response.stock.StockYearlyPriceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Component
public class KisApiClient {

    private final WebClient kisWebClient;
    private final OpenApiService openApiService;
    private final OpenApiProperties openApiProperties;
    private final ObjectMapper objectMapper;

    private static final String TR_ID = "FHKST03010100";

    public KisApiClient(
            @Qualifier("kisApiWebClient") WebClient kisWebClient,
            OpenApiService openApiService,
            OpenApiProperties openApiProperties,
            ObjectMapper objectMapper
    ) {
        this.kisWebClient = kisWebClient;
        this.openApiService = openApiService;
        this.openApiProperties = openApiProperties;
        this.objectMapper = objectMapper;
    }

    public List<StockYearlyPriceDto> getYearlyPrices(String ticker, int year) {
        return getPrices(ticker, year + "0101", year + "1231", true);
    }

    public List<StockMonthlyPriceDto> getMonthlyPrices(String ticker, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return getMonthlyDtos(ticker, start.toString().replace("-", ""), end.toString().replace("-", ""));
    }

    private List<StockYearlyPriceDto> getPrices(String ticker, String fromDate, String toDate, boolean isYearly) {
        String uri = "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";
        String accessToken = openApiService.getValidAccessToken().block();
        if (accessToken == null) {
            log.warn("[KIS API] AccessToken 획득 실패 - ticker: {}, 기간: {}~{}", ticker, fromDate, toDate);
            return List.of();
        }

        try {
            log.debug("[KIS API] 시세 요청 시작 - ticker: {}, 기간: {}~{}", ticker, fromDate, toDate);

            String rawJson = kisWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(uri)
                            .queryParam("fid_cond_mrkt_div_code", "J")
                            .queryParam("fid_input_iscd", ticker)
                            .queryParam("fid_period_div_code", isYearly ? "Y" : "M")
                            .queryParam("fid_org_adj_prc", "0")
                            .queryParam("fid_input_date_1", fromDate)
                            .queryParam("fid_input_date_2", toDate)
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
                    .bodyToMono(String.class)
                    .doOnNext(raw -> log.warn("[KIS API] 원문 응답: {}", raw))
                    .block();

            if (rawJson == null) {
                log.warn("[KIS API] 응답이 null - ticker: {}, 기간: {}~{}", ticker, fromDate, toDate);
                return List.of();
            }

            KisYearlyPriceResponseDto response = objectMapper.readValue(rawJson, KisYearlyPriceResponseDto.class);
            return response.toDtoList(ticker);

        } catch (Exception e) {
            log.error("[KIS API] 시세 조회 예외 발생 - ticker: {}, 기간: {}~{}, exception: {}", ticker, fromDate, toDate, e.getMessage(), e);
            return List.of();
        }
    }

    private List<StockMonthlyPriceDto> getMonthlyDtos(String ticker, String fromDate, String toDate) {
        String uri = "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";
        String accessToken = openApiService.getValidAccessToken().block();
        if (accessToken == null) {
            log.warn("[KIS API] AccessToken 획득 실패 - ticker: {}, 기간: {}~{}", ticker, fromDate, toDate);
            return List.of();
        }

        try {
            log.debug("[KIS API] 월봉 시세 요청 시작 - ticker: {}, 기간: {}~{}", ticker, fromDate, toDate);

            String rawJson = kisWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(uri)
                            .queryParam("fid_cond_mrkt_div_code", "J")
                            .queryParam("fid_input_iscd", ticker)
                            .queryParam("fid_period_div_code", "M")
                            .queryParam("fid_org_adj_prc", "0")
                            .queryParam("fid_input_date_1", fromDate)
                            .queryParam("fid_input_date_2", toDate)
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
                    .bodyToMono(String.class)
                    .doOnNext(raw -> log.warn("[KIS API] 월봉 원문 응답: {}", raw))
                    .block();

            if (rawJson == null) {
                log.warn("[KIS API] 월봉 응답이 null - ticker: {}, 기간: {}~{}", ticker, fromDate, toDate);
                return List.of();
            }

            KisMonthlyPriceResponseDto response = objectMapper.readValue(rawJson, KisMonthlyPriceResponseDto.class);
            return response.toMonthlyDtoList(ticker);

        } catch (Exception e) {
            log.error("[KIS API] 월봉 조회 예외 - ticker: {}, 기간: {}~{}, exception: {}", ticker, fromDate, toDate, e.getMessage(), e);
            return List.of();
        }
    }
}
