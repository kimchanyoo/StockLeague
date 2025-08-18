package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.Order;
import com.stockleague.backend.stock.domain.ReservedCash;
import com.stockleague.backend.user.domain.User;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservedCashRepository extends JpaRepository<ReservedCash, Long> {
    Optional<ReservedCash> findByOrder(Order order);

    @Query("""
        select coalesce(sum(rc.reservedAmount - rc.refundedAmount), 0)
        from ReservedCash rc
        where rc.user = :user and rc.refunded = false
    """)
    BigDecimal sumUnrefundedByUser(@Param("user") User user);
}
