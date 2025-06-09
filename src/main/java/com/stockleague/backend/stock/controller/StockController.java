package com.stockleague.backend.stock.controller;

import com.stockleague.backend.global.exception.ErrorResponse;
import com.stockleague.backend.stock.dto.response.stock.CandleDto;
import com.stockleague.backend.stock.dto.response.stock.StockListResponseDto;
import com.stockleague.backend.stock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/{ticker}/candles")
    @Operation(
            summary = "봉 데이터 조회",
            description = """
                    특정 종목에 대한 봉 데이터를 조회합니다.
                    interval 값에 따라 연봉/월봉/일봉 데이터를 반환합니다.
                    y: 연봉
                    m: 월봉
                    w: 주봉
                    d: 일봉
                    offset과 limit은 페이징 처리를 위한 값입니다.
                    offset = 0, limit = 20 → 1페이지
                    offset = 20, limit = 20 → 2페이지
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "봉 데이터 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CandleDto.class),
                                    examples = @ExampleObject(value = """
                                                [
                                                  {
                                                    "ticker": "005930",
                                                    "dateTime": "2023-01-01",
                                                    "openPrice": 70000,
                                                    "highPrice": 80000,
                                                    "lowPrice": 65000,
                                                    "closePrice": 75000,
                                                    "volume": 120000000
                                                  },
                                                  {
                                                    "ticker": "005930",
                                                    "dateTime": "2022-01-01",
                                                    "openPrice": 60000,
                                                    "highPrice": 75000,
                                                    "lowPrice": 59000,
                                                    "closePrice": 70000,
                                                    "volume": 140000000
                                                  }
                                                ]
                                            """)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "종목 정보가 존재하지 않음",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(
                                            name = "StockNotFound",
                                            summary = "해당 티커에 대한 주식 정보가 존재하지 않을 경우",
                                            value = """
                                                        {
                                                          "success": false,
                                                          "message": "해당 종목을 찾을 수 없습니다.",
                                                          "errorCode": "STOCK_NOT_FOUND"
                                                        }
                                                    """
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<List<CandleDto>> getCandles(
            @PathVariable String ticker,

            @Parameter(description = "봉 데이터 단위 (y: 연봉, m: 월봉, w: 주봉, d: 일봉)", example = "y")
            @RequestParam String interval,

            @Parameter(description = "조회 오프셋 (건 단위)", example = "0")
            @RequestParam int offset,

            @Parameter(description = "가져올 데이터 수 (limit)", example = "10")
            @RequestParam int limit
    ) {
        List<CandleDto> candles = stockService.getCandles(ticker, interval, offset, limit);
        return ResponseEntity.ok(candles);
    }
}
