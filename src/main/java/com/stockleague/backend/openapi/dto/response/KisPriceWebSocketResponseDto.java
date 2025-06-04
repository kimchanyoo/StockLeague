package com.stockleague.backend.openapi.dto.response;

import lombok.Getter;


@Getter
public class KisPriceWebSocketResponseDto {
    private Header header;
    private Body body;

    @Getter
    public static class Header {
        private String tr_id;   // 메시지 유형 ID (예: H0STCNT0)
        private String tr_key;  // 종목 코드 (예: 005930)
    }

    @Getter
    public static class Body {
        private String stck_prpr;        // 현재가
        private String prdy_vrss;        // 전일 대비
        private String prdy_vrss_sign;   // 등락 부호 (1:상승, 2:하락, 3:보합)
        private String acml_vol;         // 누적 거래량
        private String stck_oprc;        // 시가
        private String stck_hgpr;        // 고가
        private String stck_lwpr;        // 저가
        private String stck_clpr;        // 종가 (없으면 현재가로 대체)
        private String stck_bsop_date;   // 기준 일자 (yyyyMMdd)
    }
}
