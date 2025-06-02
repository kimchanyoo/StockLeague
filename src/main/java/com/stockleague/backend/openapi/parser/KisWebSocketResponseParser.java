package com.stockleague.backend.openapi.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockleague.backend.openapi.dto.response.KisPriceResponseDto;
import com.stockleague.backend.stock.dto.response.stock.StockPriceDto;
import com.stockleague.backend.stock.mapper.KisPriceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisWebSocketResponseParser {

    private final ObjectMapper objectMapper;
    private final KisPriceMapper kisPriceMapper;

    public StockPriceDto parse(String decryptedJson) {
        try {
            KisPriceResponseDto response = objectMapper.readValue(decryptedJson, KisPriceResponseDto.class);
            return kisPriceMapper.toStockPriceDto(response);
        } catch (Exception e) {
            log.error("실시간 응답 JSON 파싱 실패: {}", decryptedJson, e);
            return null;
        }
    }
}
