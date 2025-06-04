package com.stockleague.backend.openapi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.stockleague.backend.stock.dto.response.stock.StockYearlyPriceDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    public List<Output> getOutput() {
        return output;
    }

    public static class Output {
        @JsonProperty("basDt")
        public String basDt;

        @JsonProperty("mkp")
        public String mkp;

        @JsonProperty("hipr")
        public String hipr;

        @JsonProperty("lopr")
        public String lopr;

        @JsonProperty("clpr")
        public String clpr;

        @JsonProperty("acml_vol")
        public String acmlVol;
    }
}

