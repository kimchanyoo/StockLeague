package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.stock.domain.Order;
import com.stockleague.backend.stock.domain.OrderExecution;
import com.stockleague.backend.stock.domain.OrderStatus;
import com.stockleague.backend.stock.dto.response.execution.ExecutionHistoryResponseDto;
import com.stockleague.backend.stock.dto.response.execution.OrderExecutionListResponseDto;
import com.stockleague.backend.stock.dto.response.execution.OrderExecutionSummaryDto;
import com.stockleague.backend.stock.dto.response.execution.UnexecutedOrderListResponseDto;
import com.stockleague.backend.stock.dto.response.execution.UnexecutedOrderSummaryDto;
import com.stockleague.backend.stock.dto.response.order.OrderSummaryDto;
import com.stockleague.backend.stock.repository.OrderExecutionRepository;
import com.stockleague.backend.stock.repository.OrderRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderExecutionService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderExecutionRepository orderExecutionRepository;

    /**
     * 사용자의 개별 주문 상세 정보를 조회합니다.
     * <p>
     * 요청한 사용자 본인의 주문에 대해서만 상세 정보를 반환하며, 주문이 존재하지 않거나 권한이 없는 경우 예외를 발생시킵니다.
     * </p>
     *
     * @param userId  조회 대상 사용자 ID
     * @param orderId 조회 대상 주문 ID
     * @return 사용자의 주문 내역 리스트와 페이지 정보가 포함된 응답 DTO {@link OrderSummaryDto}
     * @throws GlobalException 다음과 같은 예외가 발생할 수 있습니다:
     *                         <ul>
     *                             <li>{@code ORDER_NOT_FOUND} - 주문이 존재하지 않는 경우</li>
     *                             <li>{@code UNAUTHORIZED_ORDER_ACCESS} - 다른 사용자의 주문을 취소하려는 경우</li>
     *                         </ul>
     */
    @Transactional(readOnly = true)
    public OrderExecutionListResponseDto getOrderExecutions(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userId)) {
            throw new GlobalException(GlobalErrorCode.UNAUTHORIZED_ORDER_ACCESS);
        }

        List<OrderExecution> executions = orderExecutionRepository.findByOrderOrderByExecutedAtDesc(order);

        List<OrderExecutionSummaryDto> response = executions.stream()
                .map(OrderExecutionSummaryDto::from)
                .toList();

        return OrderExecutionListResponseDto.from(response);
    }

    /**
     * 사용자의 전체 체결 내역을 페이지 단위로 조회합니다.
     * <p>
     * 체결 내역은 체결 시각(executedAt) 기준 내림차순으로 정렬되며, 매수(BUY), 매도(SELL) 구분 없이 모든 체결 내역이 포함됩니다.
     * </p>
     *
     * @param userId 조회 대상 사용자 ID
     * @param page   조회할 페이지 번호 (1부터 시작)
     * @param size   페이지당 항목 수
     * @return 사용자의 체결 내역 리스트와 페이지 정보가 포함된 응답 DTO {@link ExecutionHistoryResponseDto}
     * @throws GlobalException 다음과 같은 예외가 발생할 수 있습니다:
     *                         <ul>
     *                             <li>{@code INVALID_PAGINATION} - 페이지 번호 또는 크기가 1 미만인 경우</li>
     *                             <li>{@code USER_NOT_FOUND} - 사용자가 존재하지 않는 경우</li>
     *                         </ul>
     */
    @Transactional(readOnly = true)
    public ExecutionHistoryResponseDto listUserExecutions(Long userId, int page, int size) {
        if (page < 1 || size < 1) {
            throw new GlobalException(GlobalErrorCode.INVALID_PAGINATION);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("executedAt")));

        Page<OrderExecution> executionPage = orderExecutionRepository.findByOrderUser(user, pageable);

        List<OrderExecutionSummaryDto> contents = executionPage.getContent().stream()
                .map(OrderExecutionSummaryDto::from)
                .toList();

        return new ExecutionHistoryResponseDto(
                true,
                contents,
                page,
                size,
                executionPage.getTotalElements(),
                executionPage.getTotalPages()
        );
    }

    /**
     * 사용자의 전체 미체결 주문 내역을 페이지 단위로 조회합니다.
     * <p>
     * 미체결 주문은 상태가 {@code WAITING} 또는 {@code PARTIALLY_EXECUTED}인 주문으로 정의됩니다. 결과는 주문 생성 시각({@code orderDate}) 기준 내림차순으로
     * 정렬되어 반환됩니다. 매수(BUY), 매도(SELL) 구분 없이 모든 미체결 주문이 포함됩니다.
     * </p>
     *
     * @param userId 조회 대상 사용자 ID
     * @param page   조회할 페이지 번호 (1부터 시작)
     * @param size   페이지당 항목 수
     * @return 사용자의 미체결 주문 리스트와 페이지 정보가 포함된 응답 DTO {@link UnexecutedOrderListResponseDto}
     * @throws GlobalException 다음과 같은 예외가 발생할 수 있습니다:
     *                         <ul>
     *                             <li>{@code INVALID_PAGINATION} - 페이지 번호 또는 크기가 1 미만인 경우</li>
     *                             <li>{@code USER_NOT_FOUND} - 사용자가 존재하지 않는 경우</li>
     *                         </ul>
     */
    @Transactional(readOnly = true)
    public UnexecutedOrderListResponseDto listUnexecutedOrders(Long userId, int page, int size) {
        if (page < 1 || size < 1) {
            throw new GlobalException(GlobalErrorCode.INVALID_PAGINATION);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        List<OrderStatus> unexecutedStatuses = List.of(
                OrderStatus.WAITING,
                OrderStatus.PARTIALLY_EXECUTED
        );

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("orderDate")));
        Page<Order> orderPage = orderRepository.findByUserAndStatusIn(user, unexecutedStatuses, pageable);

        List<UnexecutedOrderSummaryDto> contents = orderPage.getContent().stream()
                .map(UnexecutedOrderSummaryDto::from)
                .toList();

        return new UnexecutedOrderListResponseDto(
                true,
                contents,
                page,
                size,
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );
    }
}
