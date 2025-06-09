package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockWeeklyPrice;
import org.springframework.data.repository.CrudRepository;

public interface StockWeeklyPriceRepository extends CrudRepository<StockWeeklyPrice, Integer> {

    boolean existsByStockAndYearAndWeek(Stock stock, int year, int week);
}
