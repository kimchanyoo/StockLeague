package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockMonthlyPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMonthlyPriceRepository extends JpaRepository<StockMonthlyPrice, Long> {
    boolean existsByStockAndYearAndMonth(Stock stock, int year, int month);
}
