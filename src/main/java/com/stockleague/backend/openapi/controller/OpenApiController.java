package com.stockleague.backend.openapi.controller;

import com.stockleague.backend.openapi.service.PriceBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/openapi")
public class OpenApiController {

    private final PriceBatchService priceBatchService;

    @PostMapping("/{ticker}/yearly-prices")
    public ResponseEntity<String> saveYearlyPrices(@PathVariable String ticker) {
        try {
            priceBatchService.saveYearlyPricesByTicker(ticker);
            return ResponseEntity.ok(ticker + " 연도별 시세 저장 완료");
        } catch (Exception e) {
            log.error("연도별 시세 저장 실패 - ticker: {}, error: {}", ticker, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("연도별 시세 저장 실패: " + e.getMessage());
        }
    }

    @PostMapping("/{ticker}/monthly-prices")
    public ResponseEntity<String> saveMonthlyPrices(@PathVariable String ticker) {
        try {
            priceBatchService.saveMonthlyPricesByTicker(ticker);
            return ResponseEntity.ok(ticker + " 월별 시세 저장 완료");
        } catch (Exception e) {
            log.error("월별 시세 저장 실패 - ticker: {}, error: {}", ticker, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("월별 시세 저장 실패: " + e.getMessage());
        }
    }
}
