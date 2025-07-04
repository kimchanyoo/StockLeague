package com.stockleague.backend.openapi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stockleague.backend.stock.dto.response.stock.StockDailyPriceDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class KisDailyPriceResponseDto {

    @JsonProperty("output2")
    private List<Output2> output;

    public List<StockDailyPriceDto> toDailyDtoList(String ticker) {
        if (output == null) {
            log.warn("[KIS API] output2가 null입니다 - ticker: {}", ticker);
            return List.of();
        }

        return output.stream()
                .map(o -> {
                    LocalDate date = parseDate(o.stckBsopDate);
                    return new StockDailyPriceDto(
                            ticker,
                            date,
                            parseLong(o.stckOprc),
                            parseLong(o.stckHgpr),
                            parseLong(o.stckLwpr),
                            parseLong(o.stckClpr),
                            parseLong(o.acmlVol)
                    );
                })
                .collect(Collectors.toList());
    }

    private LocalDate parseDate(String dateString) {
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", dateString);
            return null;
        }
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Getter
    public static class Output2 {
        @JsonProperty("stck_bsop_date")
        private String stckBsopDate;

        @JsonProperty("stck_clpr")
        private String stckClpr;

        @JsonProperty("stck_oprc")
        private String stckOprc;

        @JsonProperty("stck_hgpr")
        private String stckHgpr;

        @JsonProperty("stck_lwpr")
        private String stckLwpr;

        @JsonProperty("acml_vol")
        private String acmlVol;

        @JsonProperty("acml_tr_pbmn")
        private String acmlTrPbmn;
    }
}
