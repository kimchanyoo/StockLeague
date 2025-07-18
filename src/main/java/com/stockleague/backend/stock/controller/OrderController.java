package com.stockleague.backend.stock.controller;

import com.stockleague.backend.stock.dto.request.order.BuyOrderRequestDto;
import com.stockleague.backend.stock.dto.request.order.SellOrderRequestDto;
import com.stockleague.backend.stock.dto.response.order.BuyOrderResponseDto;
import com.stockleague.backend.stock.dto.response.order.CancelOrderResponseDto;
import com.stockleague.backend.stock.dto.response.order.OrderListResponseDto;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
@Tag(name = "Order", description = "주식 주문 관련 API")
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

    @PatchMapping("/{orderId}/cancel")
    @Operation(
            summary = "주문 취소",
            description = "사용자가 자신의 매수 또는 매도 주문을 취소합니다. " +
                    "이미 체결된 수량은 되돌릴 수 없으며, 남은 수량만 취소됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 취소 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "CancelOrderSuccess",
                                    summary = "주문 취소 성공 응답 예시",
                                    value = """
                                            {
                                                "success": true,
                                                "message": "주문이 성공적으로 취소되었습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "사용자, 주문, 자산 정보 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "OrderNotFound",
                                            summary = "존재하지 않는 주문",
                                            value = """
                                                    {
                                                        "success": false,
                                                        "message": "주문을 찾을 수 없습니다.",
                                                        "errorCode": "ORDER_NOT_FOUND"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "UnauthorizedAccess",
                                            summary = "주문에 대한 권한 없음",
                                            value = """
                                                    {
                                                        "success": false,
                                                        "message": "해당 주문에 대한 권한이 없습니다.",
                                                        "errorCode": "UNAUTHORIZED_ORDER_ACCESS"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "ReservedCashNotFound",
                                            summary = "예약된 현금 정보 없음",
                                            value = """
                                                    {
                                                        "success": false,
                                                        "message": "해당 주문의 예약 자산 정보를 찾을 수 없습니다.",
                                                        "errorCode": "RESERVED_CASH_NOT_FOUND"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "UserAssetNotFound",
                                            summary = "사용자 자산 정보 없음",
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
            @ApiResponse(responseCode = "400", description = "취소 불가능한 주문 상태",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidOrderState",
                                    summary = "취소할 수 없는 상태",
                                    value = """
                                            {
                                                "success": false,
                                                "message": "해당 주문은 취소할 수 없는 상태입니다.",
                                                "errorCode": "INVALID_ORDER_STATE"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<CancelOrderResponseDto> cancelOrder(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        CancelOrderResponseDto response = orderService.cancelOrder(userId, orderId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
            summary = "내 주문 내역 조회",
            description = "로그인한 사용자의 매수/매도 주문 내역을 페이지 단위로 조회합니다. " +
                    "주문은 작성 시각(createdAt) 기준으로 내림차순 정렬되며, 주문 상태 및 체결 정보가 포함됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 내역 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderListResponseDto.class),
                            examples = @ExampleObject(
                                    name = "OrderListSuccess",
                                    summary = "주문 내역 조회 성공 예시",
                                    value = """
                                            {
                                                "success": true,
                                                "orders": [
                                                    {
                                                        "orderId": 1,
                                                        "stockTicker": "005930",
                                                        "stockName": "삼성전자",
                                                        "orderType": "BUY",
                                                        "orderStatus": "PARTIALLY_EXECUTED",
                                                        "orderPrice": 70000,
                                                        "orderAmount": 10,
                                                        "executedAmount": 6,
                                                        "remainingAmount": 4,
                                                        "averageExecutedPrice": 69850,
                                                        "createdAt": "2025-07-15T10:24:00"
                                                    }
                                                ],
                                                "page": 1,
                                                "size": 10,
                                                "totalCount": 1,
                                                "totalPages": 1
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "페이지 요청값 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "InvalidPagination",
                                    summary = "유효하지 않은 페이지 요청",
                                    value = """
                                            {
                                                "success": false,
                                                "message": "페이지 번호 또는 크기가 유효하지 않습니다.",
                                                "errorCode": "INVALID_PAGINATION"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "사용자 정보 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "UserNotFound",
                                    summary = "존재하지 않는 사용자",
                                    value = """
                                            {
                                                "success": false,
                                                "message": "해당 사용자를 찾을 수 없습니다.",
                                                "errorCode": "USER_NOT_FOUND"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<OrderListResponseDto> getMyOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = (Long) authentication.getPrincipal();
        OrderListResponseDto response = orderService.listMyOrders(userId, page, size);

        return ResponseEntity.ok(response);
    }
}
