package com.stockleague.backend.stock.repository;

import com.stockleague.backend.stock.domain.Stock;
import com.stockleague.backend.stock.domain.StockMinutePrice;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMinutePriceRepository extends JpaRepository<StockMinutePrice, Long> {

    /**
     * 주어진 종목, 분봉 간격, 기준 시간에 해당하는 데이터가 이미 존재하는지 여부를 반환
     *
     * @param stock 종목 엔티티
     * @param interval 분봉 간격
     * @param candleTime 기준 시간
     * @return 존재 여부 (true: 존재함)
     */
    boolean existsByStockAndIntervalAndCandleTime(Stock stock, int interval, LocalDateTime candleTime);
}
