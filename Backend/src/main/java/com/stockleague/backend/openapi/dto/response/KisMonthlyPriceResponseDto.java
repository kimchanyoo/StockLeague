package com.stockleague.backend.openapi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stockleague.backend.stock.dto.response.stock.StockMonthlyPriceDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class KisMonthlyPriceResponseDto {

    @JsonProperty("output2")
    private List<Output2> output;

    public List<StockMonthlyPriceDto> toMonthlyDtoList(String ticker) {
        if (output == null) {
            log.warn("[KIS API] output2가 null입니다 - ticker: {}", ticker);
            return List.of();
        }

        return output.stream()
                .map(o -> {
                    int year = Integer.parseInt(o.stckBsopDate.substring(0, 4));
                    int month = Integer.parseInt(o.stckBsopDate.substring(4, 6));

                    return new StockMonthlyPriceDto(
                            ticker,
                            year,
                            month,
                            parseLong(o.stckOprc),
                            parseLong(o.stckHgpr),
                            parseLong(o.stckLwpr),
                            parseLong(o.stckClpr),
                            parseLong(o.acmlVol)
                    );
                })
                .collect(Collectors.toList());
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
