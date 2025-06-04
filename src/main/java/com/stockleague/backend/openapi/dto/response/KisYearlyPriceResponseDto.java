package com.stockleague.backend.openapi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stockleague.backend.stock.dto.response.stock.StockYearlyPriceDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class KisYearlyPriceResponseDto {

    @JsonProperty("output")
    private List<Output> output;

    public List<StockYearlyPriceDto> toDtoList(String ticker) {
        if (output == null) {
            log.warn("[KIS API] output이 null입니다 - ticker: {}", ticker);
            return List.of();
        }

        return output.stream()
                .map(o -> new StockYearlyPriceDto(
                        ticker,
                        Integer.parseInt(o.basDt.substring(0, 4)),
                        parseLong(o.mkp),
                        parseLong(o.hipr),
                        parseLong(o.lopr),
                        parseLong(o.clpr),
                        parseLong(o.acmlVol)
                ))
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
    public static class Output {
        @JsonProperty("basDt")
        private String basDt;

        @JsonProperty("mkp")
        private String mkp;

        @JsonProperty("hipr")
        private String hipr;

        @JsonProperty("lopr")
        private String lopr;

        @JsonProperty("clpr")
        private String clpr;

        @JsonProperty("acml_vol")
        private String acmlVol;
    }
}
