package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.Order;
import com.stockleague.backend.stock.domain.OrderStatus;
import com.stockleague.backend.user.domain.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 특정 사용자의 전체 주문 목록을 페이지 단위로 조회합니다.
     * <p>
     * 주문 상태와 관계없이 모든 주문이 포함되며,
     * 페이징 처리된 {@link Page} 객체로 반환됩니다.
     * </p>
     *
     * @param user     조회 대상 사용자
     * @param pageable 페이징 정보 (페이지 번호, 크기, 정렬 등)
     * @return 해당 사용자의 주문 목록을 담은 {@link Page<Order>} 객체
     */
    Page<Order> findByUser(User user, Pageable pageable);

    /**
     * 특정 사용자의 주문 중 지정된 상태(OrderStatus)에 해당하는 주문 목록을 조회합니다.
     * <p>
     * 주로 미체결 내역 조회에 사용되며, 예를 들어 WAITING, PARTIALLY_EXECUTED 상태만 조회할 수 있습니다.
     * </p>
     *
     * @param user     조회 대상 사용자
     * @param statuses 조회할 주문 상태 목록 (예: WAITING, PARTIALLY_EXECUTED 등)
     * @param pageable 페이징 정보
     * @return 해당 상태의 주문 목록을 담은 {@link Page<Order>} 객체
     */
    Page<Order> findByUserAndStatusIn(User user, List<OrderStatus> statuses, Pageable pageable);
}
