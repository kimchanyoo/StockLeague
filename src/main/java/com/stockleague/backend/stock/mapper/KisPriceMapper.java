package com.stockleague.backend.stock.mapper;

import com.stockleague.backend.openapi.dto.response.KisPriceResponseDto;
import com.stockleague.backend.stock.dto.response.stock.StockPriceDto;
import org.springframework.stereotype.Component;

@Component
public class KisPriceMapper {

    public StockPriceDto toStockPriceDto(KisPriceResponseDto response) {
        KisPriceResponseDto.Header header = response.getHeader();
        KisPriceResponseDto.Body body = response.getBody();

        return new StockPriceDto(
                header.getTr_key(),
                body.getStck_bsop_date(),
                parseInt(body.getStck_oprc()),
                parseInt(body.getStck_hgpr()),
                parseInt(body.getStck_lwpr()),
                parseIntOrFallback(body.getStck_clpr(), body.getStck_prpr()), // 종가 없을 경우 현재가 대체
                parseInt(body.getStck_prpr()),
                parseInt(body.getPrdy_vrss()),
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
}
