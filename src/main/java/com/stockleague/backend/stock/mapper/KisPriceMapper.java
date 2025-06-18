package com.stockleague.backend.stock.mapper;

import com.stockleague.backend.openapi.dto.response.KisPriceWebSocketResponseDto;
import com.stockleague.backend.stock.dto.response.stock.StockPriceDto;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class KisPriceMapper {

    /**
     * {@link KisPriceWebSocketResponseDto} 응답 객체를 {@link StockPriceDto}로 변환합니다.
     *
     * <p>실시간 시세 정보가 포함된 KIS WebSocket 응답 DTO에서 필요한 정보를 추출해
     * 내부 도메인 모델인 {@link StockPriceDto} 형태로 매핑</p>
     *
     * <p>종가(stck_clpr)가 누락된 경우에는 현재가(stck_prpr)로 대체하여 반환합니다.</p>
     *
     * @param response KIS WebSocket 실시간 시세 응답 DTO
     * @return 변환된 {@link StockPriceDto} 객체
     */
    public StockPriceDto toStockPriceDto(KisPriceWebSocketResponseDto response, LocalDateTime dateTime) {
        KisPriceWebSocketResponseDto.Header header = response.getHeader();
        KisPriceWebSocketResponseDto.Body body = response.getBody();

        return new StockPriceDto(
                header.getTr_key(),
                dateTime,
                parseInt(body.getStck_oprc()),
                parseInt(body.getStck_hgpr()),
                parseInt(body.getStck_lwpr()),
                parseIntOrFallback(body.getStck_clpr(), body.getStck_prpr()), // 종가 없을 경우 현재가 대체
                parseInt(body.getStck_prpr()),
                parseInt(body.getPrdy_vrss()),
                parseDouble(body.getPrdy_ctrt()),
                parseInt(body.getPrdy_vrss_sign()),
                parseLong(body.getAcml_vol())
        );
    }

    private int parseInt(String str) {
        try {
            return Integer.parseInt(str.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private int parseIntOrFallback(String str, String fallback) {
        try {
            return Integer.parseInt(str.trim());
        } catch (Exception e) {
            return parseInt(fallback);
        }
    }

    private long parseLong(String str) {
        try {
            return Long.parseLong(str.trim());
        } catch (Exception e) {
            return 0L;
        }
    }

    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
