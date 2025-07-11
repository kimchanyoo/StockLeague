package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.Order;
import com.stockleague.backend.stock.domain.ReservedCash;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservedCashRepository extends JpaRepository<ReservedCash, Long> {
    Optional<ReservedCash> findByOrder(Order order);
}
