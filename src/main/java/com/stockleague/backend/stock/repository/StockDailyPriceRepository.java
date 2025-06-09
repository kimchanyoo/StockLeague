package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockDailyPrice;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StockDailyPriceRepository extends JpaRepository<StockDailyPrice, Long> {
    boolean existsByStockAndDate(Stock stock, LocalDate date);

    @Query("""
                SELECT d
                FROM StockDailyPrice d
                WHERE d.stock.id = :stockId
                ORDER BY d.date DESC
            """)
    Page<StockDailyPrice> findAllByStockIdOrderByDateDesc(Long stockId, Pageable pageable);
}
