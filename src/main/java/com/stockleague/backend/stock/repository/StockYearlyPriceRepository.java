package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.StockYearlyPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StockYearlyPriceRepository extends JpaRepository<StockYearlyPrice, Long> {

    @Query("""
                SELECT y
                FROM StockYearlyPrice y
                WHERE y.stock.id = :stockId
                ORDER BY y.year DESC
            """)
    Page<StockYearlyPrice> findAllByStockIdOrderByYearDesc(Long stockId, Pageable pageable);
}
