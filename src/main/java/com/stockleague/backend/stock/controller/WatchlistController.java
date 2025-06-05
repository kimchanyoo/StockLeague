package com.stockleague.backend.stock.controller;

import com.stockleague.backend.global.exception.ErrorResponse;
import com.stockleague.backend.stock.dto.request.watchlist.WatchlistCreateRequestDto;
import com.stockleague.backend.stock.dto.response.watchlist.WatchlistCreateResponseDto;
import com.stockleague.backend.stock.dto.response.watchlist.WatchlistDeleteResponseDto;
import com.stockleague.backend.stock.dto.response.watchlist.WatchlistListResponseDto;
import com.stockleague.backend.stock.service.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stock/watchlist")
@Tag(name = "Watchlist", description = "관심 주식 관련 API")
public class WatchlistController {

    private final WatchlistService watchlistService;

    @PostMapping
    @Operation(summary = "관심 종목 등록", description = "관심 종목 등록을 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "관심 종목 등록 성공",
                    content = @Content(schema = @Schema(implementation = WatchlistCreateResponseDto.class),
                            examples = @ExampleObject(name = "WatchlistCreateSuccess",
                                    summary = "관심 종목 등록 완료",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "관심 항목에 등록되었습니다.",
                                              "ticker": "005930"
                                            }
                                            """
                            ))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "Missing Fields",
                                    value = """
                                                {
                                                  "success": false,
                                                  "message": "필수 입력값이 누락되었습니다.",
                                                  "errorCode": "MISSING_FIELDS"
                                                }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "사용자 또는 종목 정보를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "UserNotFound",
                                            summary = "존재하지 않는 사용자",
                                            value = """
                                                    {
                                                        "success" : false,
                                                        "message" : "해당 사용자를 찾을 수 없습니다.",
                                                        "errorCode": "USER_NOT_FOUND"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "StockNotFound",
                                            summary = "종목을 찾을 수 없는 경우",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "message": "해당 종목을 찾을 수 없습니다.",
                                                      "errorCode": "STOCK_NOT_FOUND"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<WatchlistCreateResponseDto> createWatchlist(
            @Valid @RequestBody WatchlistCreateRequestDto request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        WatchlistCreateResponseDto response = watchlistService.createWatchlist(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "관심 종목 조회", description = "관심 종목을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관심 종목 조회 성공",
                    content = @Content(schema = @Schema(implementation = WatchlistListResponseDto.class),
                            examples = @ExampleObject(name = "GetWatchlistSuccess",
                                    summary = "관심 종목 조회 완료",
                                    value = """
                                            {
                                                "success": true,
                                                "watchlists": [
                                                    {
                                                        "watchlistId": 1,
                                                        "stockId": 100,
                                                        "StockTicker": "005930",
                                                        "StockName": "삼성전자"
                                                    },
                                                    {
                                                        "watchlistId": 2,
                                                        "stockId": 101,
                                                        "StockTicker": "000660",
                                                        "StockName": "SK하이닉스"
                                                    }
                                                ],
                                                "page": 1,
                                                "size": 2,
                                                "totalCount": 12
                                                }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "사용자 정보를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "UserNotFound",
                                    summary = "존재하지 않는 사용자",
                                    value = """
                                            {
                                                "success" : false,
                                                "message" : "해당 사용자를 찾을 수 없습니다.",
                                                "errorCode": "USER_NOT_FOUND"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<WatchlistListResponseDto> getWatchlist(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        WatchlistListResponseDto response = watchlistService.getWatchlist(userId, page, size);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{watchlistId}")
    @Operation(summary = "관심 종목 삭제", description = "관심 종목을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관심 종목 삭제 성공",
                    content = @Content(schema = @Schema(implementation = WatchlistDeleteResponseDto.class),
                            examples = @ExampleObject(name = "DeleteWatchlistSuccess",
                                    summary = "관심 종목 삭제 완료",
                                    value = """
                                            {
                                                "success": true,
                                                "message": "관심 항목에서 삭제되었습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "접근 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "AccessDenied",
                                    summary = "다른 사용자의 관심 종목 삭제 시도",
                                    value = """
                                        {
                                          "success": false,
                                          "message": "접근 권한이 없습니다.",
                                          "errorCode": "FORBIDDEN"
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "사용자 또는 관심 종목 정보를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "UserNotFound",
                                            summary = "존재하지 않는 사용자",
                                            value = """
                                                    {
                                                        "success" : false,
                                                        "message" : "해당 사용자를 찾을 수 없습니다.",
                                                        "errorCode": "USER_NOT_FOUND"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "WatchlistNotFound",
                                            summary = "존재하지 않는 관심 종목",
                                            value = """
                                                    {
                                                        "success" : false,
                                                        "message" : "해당 관심 종목을 찾을 수 없습니다.",
                                                        "errorCode": "WATCHLIST_NOT_FOUND"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<WatchlistDeleteResponseDto> deleteWatchlist(
            @PathVariable Long watchlistId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        WatchlistDeleteResponseDto response = watchlistService.deleteWatchlist(userId, watchlistId);

        return ResponseEntity.ok(response);
    }
}
