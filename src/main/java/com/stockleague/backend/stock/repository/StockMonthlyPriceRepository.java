package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockMonthlyPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMonthlyPriceRepository extends JpaRepository<StockMonthlyPrice, Long> {
    boolean existsByStockAndYearAndMonth(Stock stock, int year, int month);

    @Query("""
                SELECT m
                FROM StockMonthlyPrice m
                WHERE m.stock.id = :stockId
                ORDER BY m.year DESC, m.month DESC
            """)
    Page<StockMonthlyPrice> findAllByStockIdOrderByMonthDesc(Long stockId, Pageable pageable);
}
