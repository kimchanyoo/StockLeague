package com.stockleague.backend.user.controller;

import com.stockleague.backend.user.dto.response.UserAssetValuationDto;
import com.stockleague.backend.user.service.UserAssetQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/asset")
@Tag(name = "Asset", description = "유저 자산 관련 API")
public class UserAssetController {

    private final UserAssetQueryService userAssetQueryService;

    @GetMapping
    @Operation(
            summary = "내 자산 평가 조회",
            description = "장중에는 Redis 현재가 기준으로 실시간 평가, 장 마감 이후에는 Redis 스냅샷을 기반으로 자산을 평가합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "자산 평가 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = UserAssetValuationDto.class),
                            examples = @ExampleObject(
                                    name = "UserAssetValuation",
                                    summary = "자산 평가 정보 예시",
                                    value = """
                                            {
                                                "cashBalance": "100000000.00",
                                                "totalAsset": "160000000.00",
                                                "totalProfit": "10000000.00",
                                                "totalProfitRate": "6.67",
                                                "stockValuation": "60000000.00",
                                                "isMarketOpen": true,
                                                "stocks": [
                                                    {
                                                        "ticker": "005930",
                                                        "stockName": "삼성전자",
                                                        "quantity": "100.00",
                                                        "avgBuyPrice": "65000.00",
                                                        "currentPrice": "70000.00",
                                                        "valuation": "7000000.00",
                                                        "profit": "500000.00",
                                                        "profitRate": "7.69"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자 또는 자산 정보 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "UserNotFound",
                                            summary = "존재하지 않는 사용자",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "message": "해당 사용자를 찾을 수 없습니다.",
                                                      "errorCode": "USER_NOT_FOUND"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "AssetNotFound",
                                            summary = "자산 정보 없음",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "message": "해당 유저의 자산 정보가 존재하지 않습니다.",
                                                      "errorCode": "USER_ASSET_NOT_FOUND"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Redis 역직렬화 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "RedisDeserializeError",
                                    summary = "Redis 역직렬화 오류",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Redis 데이터 역직렬화에 실패했습니다.",
                                              "errorCode": "REDIS_DESERIALIZE_ERROR"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<UserAssetValuationDto> getUserAsset(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserAssetValuationDto dto = userAssetQueryService.getUserAssetValuation(userId);

        return ResponseEntity.ok(dto);
    }
}
