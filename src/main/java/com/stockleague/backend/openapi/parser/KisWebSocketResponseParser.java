package com.stockleague.backend.openapi.parser;

import com.stockleague.backend.openapi.dto.response.KisPriceWebSocketResponseDto;
import com.stockleague.backend.stock.dto.response.stock.StockOrderBookDto;
import com.stockleague.backend.stock.dto.response.stock.StockPriceDto;
import com.stockleague.backend.stock.mapper.KisPriceMapper;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

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
                String timeStr = block[KisFieldIndex.TIME.index()];
                String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                LocalDateTime dateTime = LocalDateTime.parse(dateStr + timeStr, DATETIME_FORMATTER);

                KisPriceWebSocketResponseDto dto = mapToDto(trId, block);
                dto.getBody().setStck_bsop_date(dateStr);

                boolean isMarketOpen = isMarketTime();

                StockPriceDto stockPriceDto = kisPriceMapper.toStockPriceDto(dto, dateTime, isMarketOpen);
                result.add(stockPriceDto);
            } catch (Exception e) {
                log.error("StockPriceDto 파싱 중 예외 발생: {}", block, e);
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
     * @param trId  트랜잭션 ID
     * @param parts 실시간 평문 메시지 블록 (단일 종목의 시세 필드 배열)
     * @return 구성된 {@link KisPriceWebSocketResponseDto} 객체
     */
    private KisPriceWebSocketResponseDto mapToDto(String trId, String[] parts) {
        KisPriceWebSocketResponseDto.Header header = new KisPriceWebSocketResponseDto.Header();
        header.setTr_id(trId);
        header.setTr_key(parts[KisFieldIndex.TICKER.index()]);

        KisPriceWebSocketResponseDto.Body body = new KisPriceWebSocketResponseDto.Body();
        body.setStck_oprc(parts[KisFieldIndex.OPEN.index()]);
        body.setStck_hgpr(parts[KisFieldIndex.HIGH.index()]);
        body.setStck_lwpr(parts[KisFieldIndex.LOW.index()]);
        body.setStck_clpr(parts[KisFieldIndex.CLOSE.index()]);
        body.setStck_prpr(parts[KisFieldIndex.CURRENT_PRICE.index()]);
        body.setPrdy_vrss(parts[KisFieldIndex.PRICE_CHANGE.index()]);
        body.setPrdy_ctrt(parts[KisFieldIndex.PRICE_CHANGE_PERCENT.index()]);
        body.setPrdy_vrss_sign(parts[KisFieldIndex.CHANGE_SIGN.index()]);
        body.setAcml_vol(parts[KisFieldIndex.ACC_VOLUME.index()]);

        KisPriceWebSocketResponseDto response = new KisPriceWebSocketResponseDto();
        response.setHeader(header);
        response.setBody(body);
        return response;
    }

    /**
     * 평문으로 수신한 호가 데이터를 파싱하여 {@link StockOrderBookDto} 객체로 변환합니다.
     *
     * <p>호가 데이터는 '^' 구분자로 분리된 필드 배열로 구성되며,
     * 순서대로 매도호가, 매도잔량, 매수호가, 매수잔량이 포함됩니다.</p>
     *
     * <p>총 필드는 최소 43개 이상이어야 하며, 각 호가 및 잔량 필드는 다음 인덱스를 기준으로 파싱됩니다:</p>
     * <ul>
     *   li>매수호가: fields[3] ~ fields[12]</li>
     *   <li>매도호가: fields[13] ~ fields[22]</li>
     *   <li>매도잔량: fields[23] ~ fields[32]</li>
     *   <li>매수잔량: fields[33] ~ fields[42]</li>
     * </ul>
     *
     * @param body  실시간 호가 평문 메시지 문자열 (^ 구분자 사용)
     * @return 변환된 {@link StockOrderBookDto} 객체. 파싱 실패 시 null 반환
     */
    public StockOrderBookDto parseOrderBook(String body) {
        try {
            String[] fields = body.split("\\^");
            String ticker = fields[0];

            long[] askPrices = new long[10];
            long[] askVolumes = new long[10];
            long[] bidPrices = new long[10];
            long[] bidVolumes = new long[10];

            for (int i = 0; i < 10; i++) {
                askPrices[i] = Long.parseLong(fields[KisOrderBookFieldIndex.ASK_PRICE_START.index() + i]);
                askVolumes[i] = Long.parseLong(fields[KisOrderBookFieldIndex.ASK_VOLUME_START.index() + i]);
                bidPrices[i] = Long.parseLong(fields[KisOrderBookFieldIndex.BID_PRICE_START.index() + i]);
                bidVolumes[i] = Long.parseLong(fields[KisOrderBookFieldIndex.BID_VOLUME_START.index() + i]);
            }

            return new StockOrderBookDto(
                    ticker,
                    askPrices,
                    askVolumes,
                    bidPrices,
                    bidVolumes,
                    LocalDateTime.now()
            );

        } catch (Exception e) {
            log.error("[호가 파싱 실패]: {}", body, e);
            return null;
        }
    }

    public enum KisFieldIndex {
        TICKER(0),
        TIME(1),
        CURRENT_PRICE(2),
        CHANGE_SIGN(3),
        PRICE_CHANGE(4),
        PRICE_CHANGE_PERCENT(5),
        AVG_PRICE(6),
        OPEN(7),
        HIGH(8),
        LOW(9),
        CLOSE(10),
        ACC_VOLUME(13);

        private final int idx;

        KisFieldIndex(int idx) {
            this.idx = idx;
        }

        public int index() {
            return idx;
        }
    }

    public enum KisOrderBookFieldIndex {
        TICKER(0),

        // 실데이터 기준 인덱스
        ASK_PRICE_START(3),   // 매도호가: fields[3] ~ fields[12]
        BID_PRICE_START(13),  // 매수호가: fields[13] ~ fields[22]
        ASK_VOLUME_START(23), // 매도잔량: fields[23] ~ fields[32]
        BID_VOLUME_START(33); // 매수잔량: fields[33] ~ fields[42]


        private final int index;

        KisOrderBookFieldIndex(int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }

    private boolean isMarketTime() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek day = now.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) return false;

        LocalTime time = now.toLocalTime();
        return !time.isBefore(LocalTime.of(9, 0)) && !time.isAfter(LocalTime.of(15, 30));
    }
}
