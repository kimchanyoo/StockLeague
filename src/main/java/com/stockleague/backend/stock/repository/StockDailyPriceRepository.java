package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockDailyPrice;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockDailyPriceRepository extends JpaRepository<StockDailyPrice, Long> {
    boolean existsByStockAndDate(Stock stock, LocalDate date);
}
