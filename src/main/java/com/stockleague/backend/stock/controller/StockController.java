package com.stockleague.backend.stock.controller;

import com.stockleague.backend.stock.dto.response.stock.StockListResponseDto;
import com.stockleague.backend.stock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stocks")
@Tag(name = "Stock", description = "주식 관련 API")
public class StockController {

    private final StockService stockService;

    @GetMapping
    @Operation(
            summary = "상위 10개 종목 조회",
            description = "DB에 등록된 종목 중 상위 10개 종목을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "종목 리스트 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StockListResponseDto.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "success": true,
                                              "message": "종목 리스트 조회 성공",
                                              "stocks": [
                                                {
                                                  "stockId": 1,
                                                  "stockTicker": "005930",
                                                  "stockName": "삼성전자",
                                                  "marketType": "KOSPI"
                                                },
                                                {
                                                  "stockId": 2,
                                                  "stockTicker": "000660",
                                                  "stockName": "SK하이닉스",
                                                  "marketType": "KOSPI"
                                                }
                                              ]
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<StockListResponseDto> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }
}
