package com.stockleague.backend.stock.service;

import com.stockleague.backend.global.exception.GlobalErrorCode;
import com.stockleague.backend.global.exception.GlobalException;
import com.stockleague.backend.stock.domain.Order;
import com.stockleague.backend.stock.domain.OrderType;
import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.dto.request.order.BuyOrderRequestDto;
import com.stockleague.backend.stock.dto.response.order.BuyOrderResponseDto;
import com.stockleague.backend.stock.repository.OrderRepository;
import com.stockleague.backend.stock.repository.StockRepository;
import com.stockleague.backend.user.domain.User;
import com.stockleague.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final StockRepository stockRepository;
    private final UserRepository userRepository;

    /**
     사용자가 종목 티커(ticker)와 주문 정보(가격, 수량)를 기반으로
     * 매수 주문을 생성합니다.
     * <p>
     * 생성된 주문은 DB에 저장되며, 초기 상태는 'WAITING'입니다.
     * 체결 여부는 이후 체결 처리 로직을 통해 업데이트됩니다.
     *
     * @param userId 현재 로그인한 사용자 ID
     * @param requestDto 매수 요청 정보 (ticker, price, amount) {@link BuyOrderRequestDto}
     * @return 매수 주문 접수 결과 응답 DTO
     */
    @Transactional
    public BuyOrderResponseDto buy(Long userId, BuyOrderRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.USER_NOT_FOUND));

        Stock stock = stockRepository.findByStockTicker(requestDto.ticker())
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.STOCK_NOT_FOUND));

        Order order = Order.builder()
                .user(user)
                .stock(stock)
                .orderType(OrderType.BUY)
                .orderPrice(requestDto.orderPrice())
                .orderAmount(requestDto.orderAmount())
                .remainingAmount(requestDto.orderAmount())
                .averageExecutedPrice(requestDto.orderPrice())
                .build();

        orderRepository.save(order);

        return BuyOrderResponseDto.from();
    }
}
