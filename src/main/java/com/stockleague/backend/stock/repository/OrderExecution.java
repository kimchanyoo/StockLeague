package com.stockleague.backend.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderExecution extends JpaRepository<OrderExecution, Long> {
}
