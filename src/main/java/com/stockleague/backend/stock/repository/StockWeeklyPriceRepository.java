package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockWeeklyPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface StockWeeklyPriceRepository extends CrudRepository<StockWeeklyPrice, Integer> {

    boolean existsByStockAndYearAndWeek(Stock stock, int year, int week);

    @Query("""
            SELECT w
            FROM StockWeeklyPrice w
            WHERE w.stock.id = :stockId
            ORDER BY w.year DESC, w.week DESC
        """)
    Page<StockWeeklyPrice> findAllByStockIdOrderByWeekDesc(Long stockId, Pageable pageable);
}
