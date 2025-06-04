package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.StockYearlyPrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockYearlyPriceRepository extends JpaRepository<StockYearlyPrice, Long> {
}
