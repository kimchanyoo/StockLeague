package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.Stock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByStockTicker(String stockTicker);

    Page<Stock> findByStockNameContaining(String keyword, Pageable pageable);

    @Query("""
            SELECT s.id FROM Stock s
            WHERE s.id NOT IN (
                SELECT DISTINCT c.stock.id FROM Comment c WHERE c.deletedAt IS NULL
            )
            ORDER BY s.stockName ASC
            """)
    List<Long> findStockIdsWithoutComments(Pageable pageable);
}
