package com.stockleague.backend.openapi.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockleague.backend.openapi.dto.response.KisPriceWebSocketResponseDto;
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
            KisPriceWebSocketResponseDto response = objectMapper.readValue(decryptedJson, KisPriceWebSocketResponseDto.class);
            return kisPriceMapper.toStockPriceDto(response);
        } catch (Exception e) {
            log.error("실시간 응답 JSON 파싱 실패: {}", decryptedJson, e);
            return null;
        }
    }

    public StockPriceDto parsePlainText(String trId, String body) {
        try {
            if (!"H0STCNT0".equals(trId)) return null;

            String[] parts = body.split("\\^");
            if (parts.length < 48) {
                log.warn("H0STCNT0 필드 수 부족: {}", parts.length);
                return null;
            }

            KisPriceWebSocketResponseDto.Header header = new KisPriceWebSocketResponseDto.Header();
            KisPriceWebSocketResponseDto.Body data = new KisPriceWebSocketResponseDto.Body();

            setField(header, "tr_id", trId);
            setField(header, "tr_key", parts[0]);

            setField(data, "stck_bsop_date", "20250604");
            setField(data, "stck_oprc", parts[3]);
            setField(data, "stck_hgpr", parts[4]);
            setField(data, "stck_lwpr", parts[5]);
            setField(data, "stck_clpr", parts[6]);
            setField(data, "stck_prpr", parts[6]);
            setField(data, "prdy_vrss", parts[44]);
            setField(data, "prdy_vrss_sign", parts[46]);
            setField(data, "acml_vol", parts[47]);

            KisPriceWebSocketResponseDto response = new KisPriceWebSocketResponseDto();
            setField(response, "header", header);
            setField(response, "body", data);

            return kisPriceMapper.toStockPriceDto(response);

        } catch (Exception e) {
            log.error("실시간 평문 파싱 실패 (trId: {}): {}", trId, body, e);
            return null;
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            log.warn("필드 주입 실패 - {}.{}: {}", target.getClass().getSimpleName(), fieldName, e.getMessage());
        }
    }
}
