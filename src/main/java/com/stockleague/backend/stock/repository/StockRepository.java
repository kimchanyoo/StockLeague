package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.Stock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByStockTicker(String stockTicker);

}
