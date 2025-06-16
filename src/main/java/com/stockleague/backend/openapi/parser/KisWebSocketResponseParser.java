package com.stockleague.backend.openapi.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockleague.backend.openapi.dto.response.KisPriceWebSocketResponseDto;
import com.stockleague.backend.stock.dto.response.stock.StockPriceDto;
import com.stockleague.backend.stock.mapper.KisPriceMapper;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public List<StockPriceDto> parsePlainText(String trId, String body) {
        List<StockPriceDto> result = new ArrayList<>();
        try {
            if (!"H0STCNT0".equals(trId)) {
                log.debug("지원하지 않는 trId: {}", trId);
                return result;
            }

            String[] parts = body.split("\\^");
            int FIELD_COUNT = 48;

            log.debug("받은 body 필드 수: {}", parts.length);

            for (int i = 0; i + FIELD_COUNT <= parts.length; i += FIELD_COUNT) {
                String[] block = Arrays.copyOfRange(parts, i, i + FIELD_COUNT);
                if (block.length != FIELD_COUNT) {
                    log.warn("블록 필드 수 불일치: {}개 - {}", block.length, Arrays.toString(block));
                    continue;
                }

                StockPriceDto dto = parsePlainTextBlock(trId, block);
                if (dto != null) {
                    result.add(dto);
                } else {
                    log.warn("DTO 변환 실패: {}", Arrays.toString(block));
                }
            }

            return result;
        } catch (Exception e) {
            log.error("실시간 평문 파싱 실패 (trId: {}): {}", trId, body, e);
            return result;
        }
    }

    private StockPriceDto parsePlainTextBlock(String trId, String[] parts) {
        try {
            KisPriceWebSocketResponseDto.Header header = new KisPriceWebSocketResponseDto.Header();
            KisPriceWebSocketResponseDto.Body data = new KisPriceWebSocketResponseDto.Body();

            setField(header, "tr_id", trId);
            setField(header, "tr_key", parts[0]);

            setField(data, "stck_bsop_date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
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
            log.warn("블록 파싱 실패: {}", Arrays.toString(parts), e);
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
