package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.OrderExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderExecutionRepository extends JpaRepository<OrderExecution, Long> {
}
