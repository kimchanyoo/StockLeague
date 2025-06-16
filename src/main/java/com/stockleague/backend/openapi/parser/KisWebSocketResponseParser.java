package com.stockleague.backend.openapi.parser;

import com.stockleague.backend.openapi.dto.response.KisPriceWebSocketResponseDto;
import com.stockleague.backend.stock.dto.response.stock.StockPriceDto;
import com.stockleague.backend.stock.mapper.KisPriceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KisWebSocketResponseParser {

    private final KisPriceMapper kisPriceMapper;

    private static final int MIN_FIELD_COUNT = 46;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 평문으로 수신한 메시지 본문을 파싱하여 종목별 시세 정보를 리스트로 반환
     *
     * <p>수신된 실시간 평문 데이터는 '^' 구분자로 분리된 필드 배열이며,
     * 각 블록은 {@link StockPriceDto}로 변환함</p>
     *
     * <p>필드 수가 부족하거나 변환에 실패한 블록은 무시됨</p>
     *
     * @param trId 트랜잭션 ID (예: "H0STCNT0")
     * @param body 실시간 시세 평문 메시지 (^ 구분자 사용)
     * @return 변환된 {@link StockPriceDto} 리스트
     */
    public List<StockPriceDto> parsePlainText(String trId, String body) {
        List<StockPriceDto> result = new ArrayList<>();
        String[] parts = body.split("\\^");

        log.debug("받은 body 필드 수: {}", parts.length);

        if (parts.length < MIN_FIELD_COUNT) {
            log.warn("실시간 평문 메시지의 필드 수가 부족합니다. 수신된 필드 수: {}", parts.length);
            return result;
        }

        for (int i = 0; i + MIN_FIELD_COUNT <= parts.length; i += MIN_FIELD_COUNT) {
            String[] block = new String[MIN_FIELD_COUNT];
            System.arraycopy(parts, i, block, 0, MIN_FIELD_COUNT);

            try {
                KisPriceWebSocketResponseDto dto = mapToDto(trId, block);
                StockPriceDto stockPriceDto = kisPriceMapper.toStockPriceDto(dto);
                result.add(stockPriceDto);
            } catch (Exception e) {
                log.error("StockPriceDto 파싱 중 예외 발생: {}", (Object) block, e);
            }
        }

        if (result.isEmpty()) {
            log.debug("DTO 파싱 결과 없음 (정상적으로 파싱되지 않음)");
        }

        return result;
    }

    /**
     * 평문 메시지 블록(String 배열)을 기반으로 {@link KisPriceWebSocketResponseDto} 객체로 매핑합니다.
     *
     * <p>내부적으로 각 필드 인덱스를 enum {@link KisFieldIndex}를 통해 참조하며,
     * 실시간 시세를 JSON 기반 DTO 객체 구조로 구성해 반환합니다.</p>
     *
     * @param trId 트랜잭션 ID
     * @param parts 실시간 평문 메시지 블록 (단일 종목의 시세 필드 배열)
     * @return 구성된 {@link KisPriceWebSocketResponseDto} 객체
     */
    private KisPriceWebSocketResponseDto mapToDto(String trId, String[] parts) {
        KisPriceWebSocketResponseDto.Header header = new KisPriceWebSocketResponseDto.Header();
        header.setTr_id(trId);
        header.setTr_key(parts[KisFieldIndex.TICKER.ordinal()]);

        KisPriceWebSocketResponseDto.Body body = new KisPriceWebSocketResponseDto.Body();
        body.setStck_bsop_date(LocalDate.now().format(DATE_FORMATTER));
        body.setStck_oprc(parts[KisFieldIndex.OPEN.ordinal()]);
        body.setStck_hgpr(parts[KisFieldIndex.HIGH.ordinal()]);
        body.setStck_lwpr(parts[KisFieldIndex.LOW.ordinal()]);
        body.setStck_clpr(parts[KisFieldIndex.CLOSE.ordinal()]);
        body.setStck_prpr(parts[KisFieldIndex.CURRENT_PRICE.ordinal()]);
        body.setPrdy_vrss(parts[KisFieldIndex.PRICE_CHANGE.ordinal()]);
        body.setPrdy_ctrt(parts[KisFieldIndex.PRICE_CHANGE_PERCENT.ordinal()]);
        body.setPrdy_vrss_sign(parts[KisFieldIndex.CHANGE_SIGN.ordinal()]);
        body.setAcml_vol(parts[KisFieldIndex.ACC_VOLUME.ordinal()]);

        KisPriceWebSocketResponseDto response = new KisPriceWebSocketResponseDto();
        response.setHeader(header);
        response.setBody(body);
        return response;
    }

    public enum KisFieldIndex {
        TICKER,               // 0 종목코드
        TIME,                 // 1 체결시간
        CURRENT_PRICE,        // 2 현재가
        CHANGE_SIGN,          // 3 등락부호
        PRICE_CHANGE,         // 4 전일 대비 가격
        PRICE_CHANGE_PERCENT, // 5 전일 대비 퍼센트
        OPEN,                 // 6 시가
        HIGH,                 // 7 고가
        LOW,                  // 8 저가
        CLOSE,                // 9 종가
        ACC_VOLUME            // 10 누적 거래량
    }
}
