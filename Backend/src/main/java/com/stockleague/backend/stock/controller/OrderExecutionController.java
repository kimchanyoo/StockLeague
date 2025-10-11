package com.stockleague.backend.stock.controller;

import com.stockleague.backend.stock.dto.response.execution.ExecutionHistoryResponseDto;
import com.stockleague.backend.stock.dto.response.execution.OrderExecutionListResponseDto;
import com.stockleague.backend.stock.dto.response.execution.UnexecutedOrderListResponseDto;
import com.stockleague.backend.stock.service.OrderExecutionService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/executions")
@Tag(name = "Execution", description = "주식 체결 관련 API")
public class OrderExecutionController {

    private final OrderExecutionService orderExecutionService;

    @GetMapping("/{orderId}/executions")
    @Operation(
            summary = "주문 체결 내역 조회",
            description = "특정 주문 ID에 대한 체결 내역을 조회합니다. " +
                    "체결이 이루어진 경우, 체결 가격, 수량, 시각 등의 정보를 포함한 리스트를 반환합니다. " +
                    "체결 내역이 없는 경우 빈 배열이 반환됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "체결 내역 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderExecutionListResponseDto.class),
                            examples = @ExampleObject(
                                    name = "OrderExecutionListSuccess",
                                    summary = "체결 내역 조회 성공 예시",
                                    value = """
                                            {
                                                "success": true,
                                                "contents": [
                                                    {
                                                        "orderExecutionId": 1001,
                                                        "stockName": "삼성전자",
                                                        "orderType": "BUY",
                                                        "executedPrice": 69800,
                                                        "executedAmount": 5,
                                                        "executedAt": "2025-07-15T10:25:30"
                                                    },
                                                    {
                                                        "orderExecutionId": 1002,
                                                        "stockName": "삼성전자",
                                                        "orderType": "BUY",
                                                        "executedPrice": 69700,
                                                        "executedAmount": 1,
                                                        "executedAt": "2025-07-15T10:26:15"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "주문 정보 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "OrderNotFound",
                                    summary = "존재하지 않는 주문",
                                    value = """
                                            {
                                                "success": false,
                                                "message": "주문을 찾을 수 없습니다.",
                                                "errorCode": "ORDER_NOT_FOUND"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "주문 접근 권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "UnauthorizedOrderAccess",
                                    summary = "해당 주문에 대한 권한 없음",
                                    value = """
                                            {
                                                "success": false,
                                                "message": "해당 주문에 대한 권한이 없습니다.",
                                                "errorCode": "UNAUTHORIZED_ORDER_ACCESS"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<OrderExecutionListResponseDto> getOrderExecutions(
            Authentication authentication,
            @PathVariable Long orderId
    ) {
        Long userId = (Long) authentication.getPrincipal();

        OrderExecutionListResponseDto response = orderExecutionService.getOrderExecutions(userId, orderId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
            summary = "전체 체결 내역 조회",
            description = "로그인한 사용자의 매수/매도 체결 내역 전체를 페이지 단위로 조회합니다. " +
                    "체결 시각(executedAt) 기준으로 내림차순 정렬되며, 체결가, 수량 등의 정보가 포함됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "체결 내역 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExecutionHistoryResponseDto.class),
                            examples = @ExampleObject(
                                    name = "ExecutionHistorySuccess",
                                    summary = "체결 내역 조회 성공 예시",
                                    value = """
                                            {
                                                "success": true,
                                                "contents": [
                                                    {
                                                        "orderExecutionId": 1,
                                                        "stockName": "삼성전자",
                                                        "orderType": "BUY",
                                                        "executedPrice": 71000,
                                                        "executedAmount": 5,
                                                        "executedAt": "2025-07-15T13:15:00"
                                                    },
                                                    {
                                                        "orderExecutionId": 2,
                                                        "stockName": "삼성전자",
                                                        "orderType": "SELL",
                                                        "executedPrice": 72000,
                                                        "executedAmount": 3,
                                                        "executedAt": "2025-07-15T13:20:00"
                                                    }
                                                ],
                                                "page": 1,
                                                "size": 10,
                                                "totalCount": 2,
                                                "totalPage": 1
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 페이지 요청값",
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
    public ResponseEntity<ExecutionHistoryResponseDto> getExecutionHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = (Long) authentication.getPrincipal();
        ExecutionHistoryResponseDto response = orderExecutionService.listUserExecutions(userId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unexecuted")
    @Operation(
            summary = "미체결 주문 목록 조회",
            description = "사용자의 미체결 주문(WAITING, PARTIALLY_EXECUTED 상태)을 페이지 단위로 조회합니다. " +
                    "주문 ID, 종목명, 주문가, 주문 수량, 잔여 수량, 주문 상태 등의 정보를 포함합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "미체결 주문 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UnexecutedOrderListResponseDto.class),
                            examples = @ExampleObject(
                                    name = "UnexecutedOrderListSuccess",
                                    summary = "미체결 주문 목록 예시",
                                    value = """
                                        {
                                            "success": true,
                                            "contents": [
                                                {
                                                    "orderId": 101,
                                                    "stockName": "삼성전자",
                                                    "stockTicker": "005930",
                                                    "orderType": "BUY",
                                                    "orderPrice": 70000,
                                                    "orderAmount": 10,
                                                    "remainingAmount": 5,
                                                    "status": "PARTIALLY_EXECUTED",
                                                    "createdAt": "2025-07-15T13:10:00"
                                                },
                                                {
                                                    "orderId": 102,
                                                    "stockName": "LG에너지솔루션",
                                                    "stockTicker": "373220",
                                                    "orderType": "SELL",
                                                    "orderPrice": 580000,
                                                    "orderAmount": 3,
                                                    "remainingAmount": 3,
                                                    "status": "WAITING",
                                                    "createdAt": "2025-07-15T13:20:00"
                                                }
                                            ],
                                            "page": 1,
                                            "size": 10,
                                            "totalCount": 2,
                                            "totalPage": 1
                                        }
                                        """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 페이지 요청값",
                    content = @Content(
                            mediaType = "application/json",
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
                    content = @Content(
                            mediaType = "application/json",
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
    public ResponseEntity<UnexecutedOrderListResponseDto> getUnexecutedOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = (Long) authentication.getPrincipal();
        UnexecutedOrderListResponseDto response = orderExecutionService.listUnexecutedOrders(userId, page, size);
        return ResponseEntity.ok(response);
    }
}
