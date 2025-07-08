package com.stockleague.backend.stock.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderExecutionRepository extends JpaRepository<OrderExecutionRepository, Long> {
}
