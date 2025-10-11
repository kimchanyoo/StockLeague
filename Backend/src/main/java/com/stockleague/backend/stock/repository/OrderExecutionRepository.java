package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.Order;
import com.stockleague.backend.stock.domain.OrderExecution;
import com.stockleague.backend.user.domain.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderExecutionRepository extends JpaRepository<OrderExecution, Long> {

    /**
     * 특정 주문에 대한 모든 체결 내역을 조회합니다.
     *
     * @param order 조회할 주문
     * @return 해당 주문의 체결 내역 리스트
     */
    List<OrderExecution> findByOrderOrderByExecutedAtDesc(Order order);

    /**
     *  해당 유저에 대한 모든 체결 내역을 조회합니다.
     * @param user 조회할 유저
     * @param pageable 페이지 조건
     * @return 해당 체결 내역 페이지 리스트
     */
    Page<OrderExecution> findByOrderUser(User user, Pageable pageable);
}
