package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.StockMinutePrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMinutePriceRepository extends JpaRepository<StockMinutePrice, Long> {

}
