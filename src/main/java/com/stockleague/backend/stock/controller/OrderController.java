package com.stockleague.backend.stock.controller;

import com.stockleague.backend.stock.dto.request.order.BuyOrderRequestDto;
import com.stockleague.backend.stock.dto.request.order.SellOrderRequestDto;
import com.stockleague.backend.stock.dto.response.order.BuyOrderResponseDto;
import com.stockleague.backend.stock.dto.response.order.SellOrderResponseDto;
import com.stockleague.backend.stock.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
@Tag(name = "Order", description = "주식 체결 관련 API")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/buy")
    @Operation(
            summary = "주식 매수 주문",
            description = "사용자가 특정 종목에 대해 매수 주문을 요청합니다. " +
                    "즉시 체결 가능한 경우 체결되고, 그렇지 않으면 대기열에 등록됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매수 주문 성공",
                    content = @Content(schema = @Schema(implementation = BuyOrderResponseDto.class),
                            examples = @ExampleObject(
                                    name = "BuyOrderSuccess",
                                    summary = "주문 성공 응답 예시",
                                    value = """
                                            {
                                                "success": true,
                                                "message": "매수 주문이 정상적으로 접수되었습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "해당 사용자가 없음",
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
                                            summary = "자산이 존재하지 않음",
                                            value = """
                                                    {
                                                    "success" : false,
                                                    "message" : "해당 주식 정보가 존재하지 않습니다.",
                                                    "errorCode": "STOCK_NOT_FOUND"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<BuyOrderResponseDto> buyStock(
            Authentication authentication,
            @Valid @RequestBody BuyOrderRequestDto requestDto
    ) {
        Long userId = (Long) authentication.getPrincipal();

        BuyOrderResponseDto response = orderService.buy(userId, requestDto);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/sell")
    @Operation(
            summary = "주식 매도 주문",
            description = "사용자가 특정 종목에 대해 매도 주문을 요청합니다. " +
                    "즉시 체결 가능한 경우 체결되고, 그렇지 않으면 대기열에 등록됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매도 주문 성공",
                    content = @Content(schema = @Schema(implementation = SellOrderResponseDto.class),
                            examples = @ExampleObject(
                                    name = "SellOrderSuccess",
                                    summary = "주문 성공 응답 예시",
                                    value = """
                                        {
                                            "success": true,
                                            "message": "매도 주문이 정상적으로 접수되었습니다."
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "사용자 또는 종목 정보 없음",
                    content = @Content(mediaType = "application/json",
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
                                            name = "StockNotFound",
                                            summary = "존재하지 않는 종목",
                                            value = """
                                                {
                                                    "success": false,
                                                    "message": "해당 주식 정보가 존재하지 않습니다.",
                                                    "errorCode": "STOCK_NOT_FOUND"
                                                }
                                                """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<SellOrderResponseDto> sellStock(
            Authentication authentication,
            @Valid @RequestBody SellOrderRequestDto requestDto
    ) {
        Long userId = (Long) authentication.getPrincipal();
        SellOrderResponseDto response = orderService.sell(userId, requestDto);
        return ResponseEntity.ok(response);
    }
}
