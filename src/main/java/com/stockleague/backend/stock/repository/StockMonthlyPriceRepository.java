package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.StockMonthlyPrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMonthlyPriceRepository extends JpaRepository<StockMonthlyPrice, Long> {
}
